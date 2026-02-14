package com.htc.enter.serviceimpl;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.dto.EpicDTO;
import com.htc.enter.model.Epic;
import com.htc.enter.model.Project;
import com.htc.enter.model.User;
import com.htc.enter.repository.ProjectRepository;
import com.htc.enter.repository.UserRepository;
import com.htc.enter.repository.EpicRepository;
import com.htc.enter.service.EpicService;
import com.htc.enter.notification.NotificationService;
import com.htc.enter.service.NotificationDatabaseService;
import com.htc.enter.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EpicServImpl implements EpicService {
    private static final Logger log = LoggerFactory.getLogger(EpicServImpl.class);

    private final EpicRepository repo;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final NotificationDatabaseService notificationDatabaseService;

    public EpicServImpl(EpicRepository repo, ProjectRepository projectRepo, 
                       UserRepository userRepo, NotificationService notificationService,
                       NotificationDatabaseService notificationDatabaseService) {
        this.repo = repo;
        this.projectRepo = projectRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
        this.notificationDatabaseService = notificationDatabaseService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "epics", allEntries = true)
    public Epic save(Epic workflowStates) {
        return repo.save(workflowStates);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "epics", key = "#id")
    public Epic findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "epics")
    public List<Epic> findAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "epics", allEntries = true)
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "epics", allEntries = true)
    public Epic createFromDTO(EpicDTO dto) {
        Epic ws = new Epic();
        ws.setName(dto.getName());
        ws.setIs_start(dto.getIsStart());
        // map deadline and deliverables; do NOT accept is_end from the DTO (it's set when approved)
        if (dto.getDeadline() != null) ws.setDeadline(dto.getDeadline());
        ws.setDeliverables(dto.getDeliverables());
        ws.setIs_approved(dto.getIsApproved() != null ? dto.getIsApproved() : false);
        if (dto.getProjectId() != null) {
            Project p = projectRepo.findById(dto.getProjectId()).orElse(null);
            ws.setProjectId(p);
        }
        if (dto.getManagerId() != null) {
            User manager = userRepo.findById(dto.getManagerId()).orElse(null);
            ws.setManager_id(manager);
        }
        if (dto.getCreatedById() != null) {
            User creator = userRepo.findById(dto.getCreatedById()).orElse(null);
            ws.setCreated_by(creator);
        }
        Epic saved = repo.save(ws);
        // Create database notification for epic creation
        try {
            User manager = saved.getManager_id();
            if (manager != null) {
                notificationDatabaseService.createNotification(
                    manager.getId(),
                    "New Epic Created",
                    "A new epic has been created: " + saved.getName(),
                    Notification.NotificationType.EPIC_CREATED,
                    "EPIC",
                    saved.getEpicId()
                );
            }
        } catch (Exception e) {
            log.warn("Failed to create epic-created notification: {}", e.getMessage());
        }
        if (saved.isIs_approved()) notificationService.notifyEpicApproved(saved);
        if (saved.isIs_approved()) {
            try {
                User manager = saved.getManager_id();
                if (manager != null) {
                    notificationDatabaseService.createNotification(
                        manager.getId(),
                        "Epic Approved",
                        "Your epic '" + saved.getName() + "' has been approved",
                        Notification.NotificationType.EPIC_APPROVED,
                        "EPIC",
                        saved.getEpicId()
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to create epic-approved notification: {}", e.getMessage());
            }
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "epics", allEntries = true)
    public Epic updateFromDTO(Long id, EpicDTO dto) {
        Epic existing = findById(id);
        if (existing == null) throw new IllegalArgumentException("Workflow state not found with id: " + id);
        boolean wasApproved = existing.isIs_approved();
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getIsStart() != null) existing.setIs_start(dto.getIsStart());
        // do not map isEnd directly; only allow updating the deadline
        if (dto.getDeadline() != null) existing.setDeadline(dto.getDeadline());
        if (dto.getDeliverables() != null) existing.setDeliverables(dto.getDeliverables());
        if (dto.getIsApproved() != null) existing.setIs_approved(dto.getIsApproved());
        if (dto.getProjectId() != null) {
            Project p = projectRepo.findById(dto.getProjectId()).orElse(null);
            existing.setProjectId(p);
        }
        if (dto.getManagerId() != null) {
            User manager = userRepo.findById(dto.getManagerId()).orElse(null);
            existing.setManager_id(manager);
        }
        if (dto.getCreatedById() != null) {
            User creator = userRepo.findById(dto.getCreatedById()).orElse(null);
            existing.setCreated_by(creator);
        }
        Epic saved = repo.save(existing);
        if (!wasApproved && saved.isIs_approved()) notificationService.notifyEpicApproved(saved);
        if (!wasApproved && saved.isIs_approved()) {
            try {
                User manager = saved.getManager_id();
                if (manager != null) {
                    notificationDatabaseService.createNotification(
                        manager.getId(),
                        "Epic Approved",
                        "Your epic '" + saved.getName() + "' has been approved",
                        Notification.NotificationType.EPIC_APPROVED,
                        "EPIC",
                        saved.getEpicId()
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to create epic-approved notification: {}", e.getMessage());
            }
        }
        if (saved.isIs_approved() && saved.getIs_end() != null) notificationService.notifyEpicFinished(saved);
        if (saved.isIs_approved() && saved.getIs_end() != null) {
            try {
                User manager = saved.getManager_id();
                if (manager != null) {
                    notificationDatabaseService.createNotification(
                        manager.getId(),
                        "Epic Completed",
                        "Your epic '" + saved.getName() + "' has been completed",
                        Notification.NotificationType.EPIC_COMPLETED,
                        "EPIC",
                        saved.getEpicId()
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to create epic-completed notification: {}", e.getMessage());
            }
        }
        return saved;
    }
}