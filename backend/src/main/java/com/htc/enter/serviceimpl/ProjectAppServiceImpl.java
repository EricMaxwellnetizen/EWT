package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.dto.DocumentPayload;
import com.htc.enter.dto.ProjectDTO;
import com.htc.enter.model.Project;
import com.htc.enter.model.User;
import com.htc.enter.service.ProjectAppService;
import com.htc.enter.service.ProjectService;
import com.htc.enter.service.ProjectDocumentService;
import com.htc.enter.service.ProjectNotificationService;
import com.htc.enter.service.UserAuthService;
import com.htc.enter.service.NotificationDatabaseService;
import com.htc.enter.model.Notification;
import com.htc.enter.util.DocumentPasswordUtil;

@Service
public class ProjectAppServiceImpl implements ProjectAppService {

    private static final Logger log = LoggerFactory.getLogger(ProjectAppServiceImpl.class);

    private final ProjectService projectService;
    private final ProjectDocumentService documentService;
    private final ProjectNotificationService notificationService;
    private final UserAuthService authService;
    private final NotificationDatabaseService notificationDatabaseService;

    public ProjectAppServiceImpl(ProjectService projectService,
                                 ProjectDocumentService documentService,
                                 ProjectNotificationService notificationService,
                                 UserAuthService authService,
                                 NotificationDatabaseService notificationDatabaseService) {
        this.projectService = projectService;
        this.documentService = documentService;
        this.notificationService = notificationService;
        this.authService = authService;
        this.notificationDatabaseService = notificationDatabaseService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project createProject(ProjectDTO dto) {
        Project saved = projectService.createFromDTO(dto);
        // Send email notification to assigned manager
        try {
            User manager = saved.getManager_id();
            if (manager != null && manager.getEmail() != null && !manager.getEmail().isBlank()) {
                String password = manager.getUsername() + "123";
                ByteArrayOutputStream docStream = documentService.generatePasswordProtectedProjectDocument(saved, password);
                String subject = "New Project Assigned: " + saved.getName();
                String status = saved.isIs_approved() ? "Approved" : "Pending Approval";
                String body = String.format(
                    "Dear %s,\n\nYou have been assigned as the manager for a new project:\n\n" +
                    "Project Name: %s\nProject ID: %d\nStatus: %s\nDeadline: %s\nDeliverables: %s\n\n" +
                    "The attached Word document contains full project details and is password-protected.\nPassword: %s\n\n" +
                    "Best regards,\nElara",
                    manager.getUsername(),
                    saved.getName(),
                    saved.getProjectId(),
                    status,
                    saved.getDeadline() != null ? saved.getDeadline().toString() : "Not set",
                    saved.getDeliverables() != null ? saved.getDeliverables() : "Not specified",
                    password
                );
                String attachmentName = "project_" + saved.getProjectId() + "_details.docx";
                notificationService.sendEmailWithAttachment(manager.getEmail(), subject, body, attachmentName, docStream);
            }
        } catch (Exception e) {
            log.warn("Failed to send project-created email: {}", e.getMessage());
        }
        // Create database notification for manager
        try {
            User manager = saved.getManager_id();
            if (manager != null) {
                notificationDatabaseService.createNotification(
                    manager.getId(),
                    "New Project Assigned",
                    "You have been assigned as manager for project: " + saved.getName(),
                    Notification.NotificationType.PROJECT_CREATED,
                    "PROJECT",
                    saved.getProjectId()
                );
            }
        } catch (Exception e) {
            log.warn("Failed to create project-created notification: {}", e.getMessage());
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project updateProject(Long id, ProjectDTO dto) {
        Project existing = projectService.findById(id);
        boolean wasApproved = existing != null && existing.isIs_approved();
        Project updated = projectService.updateFromDTO(id, dto);

        if (!wasApproved && updated.isIs_approved()) {
            try {
                User manager = updated.getManager_id();
                if (manager != null && manager.getEmail() != null && !manager.getEmail().isBlank()) {
                    String password = manager.getUsername() + "123";
                    ByteArrayOutputStream docStream = documentService.generatePasswordProtectedProjectDocument(updated, password);
                    String subject = "Project Approved: " + updated.getName();
                    String body = String.format(
                        "Dear %s,\n\nA project has been approved with the following details:\n\n" +
                        "Project Name: %s\nProject ID: %d\nDeadline: %s\nDeliverables: %s\n\n" +
                        "The attached Word document is password-protected. Use password: %s\n\nRegards,\nElara",
                        manager.getUsername(),
                        updated.getName(),
                        updated.getProjectId(),
                        updated.getDeadline() != null ? updated.getDeadline().toString() : "Not set",
                        updated.getDeliverables() != null ? updated.getDeliverables() : "Not specified",
                        password
                    );
                    String attachmentName = "project_" + updated.getProjectId() + "_details.docx";
                    notificationService.sendEmailWithAttachment(manager.getEmail(), subject, body, attachmentName, docStream);
                }
            } catch (Exception e) {
                log.warn("Failed to send project-approved email: {}", e.getMessage());
            }
            // Create database notification for project update
            try {
                User manager = updated.getManager_id();
                if (manager != null) {
                    notificationDatabaseService.createNotification(
                        manager.getId(),
                        "Project Approved",
                        "Your project '" + updated.getName() + "' has been approved",
                        Notification.NotificationType.PROJECT_UPDATED,
                        "PROJECT",
                        updated.getProjectId()
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to create project-approved notification: {}", e.getMessage());
            }
        }

        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProject(Long id) {
        projectService.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentPayload buildManagerProjectsDocument(Long managerId) throws IOException {
        List<Project> projects = projectService.findByManagerId(managerId);
        User manager = null;
        if (projects != null && !projects.isEmpty()) manager = projects.get(0).getManager_id();
        User current = authService.getCurrentUser();
        String password = DocumentPasswordUtil.resolvePassword(manager, current);

        ByteArrayOutputStream os = documentService.generatePasswordProtectedManagerProjectsDocument(managerId,
                projects, password);
        String fileName = "manager_" + managerId + "_projects.docx";
        return DocumentPayload.ofDocx(os.toByteArray(), fileName);
    }
}