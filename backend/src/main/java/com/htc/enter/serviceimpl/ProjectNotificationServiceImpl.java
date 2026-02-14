package com.htc.enter.serviceimpl;

import java.io.ByteArrayOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.htc.enter.model.Project;
import com.htc.enter.model.User;
import com.htc.enter.service.ProjectDocumentService;
import com.htc.enter.service.ProjectNotificationService;

import jakarta.mail.internet.MimeMessage;

@Service
public class ProjectNotificationServiceImpl implements ProjectNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ProjectNotificationServiceImpl.class);
    
    private final JavaMailSender mailSender;
    private final ProjectDocumentService documentService;
    private final String fromAddress;
    private final boolean notificationsEnabled;

    public ProjectNotificationServiceImpl(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            ProjectDocumentService documentService,
            @Value("${notification.from:}") String fromAddress,
            @Value("${notification.enabled:true}") boolean notificationsEnabled) {
        this.mailSender = mailSenderProvider.getIfAvailable();
        this.documentService = documentService;
        this.fromAddress = fromAddress;
        this.notificationsEnabled = notificationsEnabled;
        
        if (!this.notificationsEnabled) {
            log.info("Project notification emails are disabled via 'notification.enabled' property; emails will be skipped.");
        } else if (this.mailSender == null) {
            log.warn("JavaMailSender bean not found — project notifications will be logged but not sent.");
        }
    }

    @Async
    @Override
    public void notifyManagerOnProjectCreation(Project project) {
        if (!notificationsEnabled) {
            log.debug("Skipping project creation notification because notifications are disabled.");
            return;
        }
        if (project == null || project.getManager_id() == null) {
            log.warn("Cannot send notification: project or manager is null");
            return;
        }
        
        User manager = project.getManager_id();
        if (manager.getEmail() == null || manager.getEmail().isBlank()) {
            log.warn("Cannot send notification: manager {} has no email", manager.getUsername());
            return;
        }
        
        // Send notification to the manager's email address
        String recipientEmail = manager.getEmail();
        
        try {
            // Generate password: username + "123"
            String password = manager.getUsername() + "123";
            
            // Generate password-protected document
            ByteArrayOutputStream docStream = documentService.generatePasswordProtectedProjectDocument(project, password);
            
            // Prepare email
            String subject = "New Project Assigned: " + project.getName();
            String body = String.format(
                "Dear %s,\n\n" +
                "A new project has been assigned to you:\n\n" +
                "Project Name: %s\n" +
                "Project ID: %d\n" +
                "Deadline: %s\n" +
                "Deliverables: %s\n\n" +
                "Please find the detailed project information in the attached password-protected document.\n" +
                "Password: %s\n\n" +
                "Best regards,\n" +
                "Elara",
                manager.getUsername(),
                project.getName(),
                project.getProjectId(),
                project.getDeadline() != null ? project.getDeadline().toString() : "Not set",
                project.getDeliverables() != null ? project.getDeliverables() : "Not specified",
                password
            );
            
            String attachmentName = "project_" + project.getProjectId() + "_details.docx";
            sendEmailWithAttachment(recipientEmail, subject, body, attachmentName, docStream);
            
            log.info("Project creation notification sent to manager {} for project {}", 
                     manager.getUsername(), project.getProjectId());
                     
        } catch (Exception e) {
            log.error("Failed to send project creation notification to manager {}: {}", 
                     manager.getUsername(), e.getMessage(), e);
        }
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, 
                                       String attachmentName, ByteArrayOutputStream attachmentContent) {
        if (!notificationsEnabled) {
            log.debug("Skipping email to {} because notifications are disabled.", to);
            return;
        }
        if (mailSender == null) {
            log.info("[NO-SMTP] Would send email with attachment to {} subject={} attachment={}", 
                    to, subject, attachmentName);
            return;
        }
        if (attachmentContent == null) {
            log.warn("Attachment content is null; skipping email to {} subject={}", to, subject);
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress);
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            
            // Add attachment
            helper.addAttachment(attachmentName, () -> 
                new java.io.ByteArrayInputStream(attachmentContent.toByteArray()));
            
            mailSender.send(message);
            log.debug("Sent email with attachment to {} subject={} attachment={}", to, subject, attachmentName);
            
        } catch (Exception e) {
            log.error("Failed to send email with attachment to {}: {}", to, e.getMessage(), e);
        }
    }
}