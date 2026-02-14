package com.htc.enter.serviceimpl;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.htc.enter.dto.StoryDTO;
import com.htc.enter.model.Story;
import com.htc.enter.model.Project;
import com.htc.enter.model.User;
import com.htc.enter.model.Epic;
import com.htc.enter.repository.ProjectRepository;
import com.htc.enter.repository.StoryRepository;
import com.htc.enter.repository.UserRepository;
import com.htc.enter.repository.EpicRepository;
import com.htc.enter.service.StoryService;
import com.htc.enter.notification.NotificationService;
import com.htc.enter.service.NotificationDatabaseService;
import com.htc.enter.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

@Service
public class StoryServiceImpl implements StoryService {
    private static final Logger log = LoggerFactory.getLogger(StoryServiceImpl.class);

    private final StoryRepository repo;
    private final ProjectRepository projectRepo;
    private final EpicRepository stateRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final NotificationDatabaseService notificationDatabaseService;

    public StoryServiceImpl(StoryRepository repo, ProjectRepository projectRepo, 
                           EpicRepository stateRepo, UserRepository userRepo, 
                           NotificationService notificationService,
                           NotificationDatabaseService notificationDatabaseService) {
        this.repo = repo;
        this.projectRepo = projectRepo;
        this.stateRepo = stateRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
        this.notificationDatabaseService = notificationDatabaseService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "stories", allEntries = true)
    public Story save(Story story) {
        return repo.save(story);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "stories", key = "#id")
    public Story findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "stories")
    public List<Story> findAll() {
        return repo.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "stories", allEntries = true)
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "stories", allEntries = true)
    public Story createFromDTO(StoryDTO dto) {
        Story t = new Story();
        t.setTitle(dto.getTitle());
        t.setDeliverables(dto.getDescription());
        t.setDueDate(dto.getDueDate());
        // map optional: deadline
        try { if (dto.getDeadline() != null) t.setDeadline(dto.getDeadline()); } catch (Exception e) { }
        // map approval flag and isEnd when appropriate
        try { t.setIs_approved(dto.getIsApproved() != null ? dto.getIsApproved() : false); } catch (Exception e) { }
        try { if (dto.getIsEnd() != null) t.setIs_end(dto.getIsEnd()); } catch (Exception e) { }

        if (dto.getProjectId() != null) {
            Project p = projectRepo.findById(dto.getProjectId()).orElse(null);
            t.setProjectId(p);
        }
        if (dto.getWorkflowStateId() != null) {
            Epic ws = stateRepo.findById(dto.getWorkflowStateId()).orElse(null);
            t.setEpicId(ws);
        }
        if (dto.getAssignedToId() != null) {
            User u = userRepo.findById(dto.getAssignedToId()).orElse(null);
            t.setAssigned_to(u);
        }
        if (dto.getCreatedById() != null) {
            User creator = userRepo.findById(dto.getCreatedById()).orElse(null);
            t.setCreated_by(creator);
        }
        Story saved = repo.save(t);
        // Notify assigned user immediately when story is created
        if (saved.getAssigned_to() != null) {
            notificationService.notifyStoryAssigned(saved);
            // Create database notification for story assignment
            try {
                notificationDatabaseService.createNotification(
                    saved.getAssigned_to().getId(),
                    "New Story Assigned",
                    "You have been assigned to story: " + saved.getTitle(),
                    Notification.NotificationType.STORY_ASSIGNED,
                    "STORY",
                    saved.getStoryId()
                );
            } catch (Exception e) {
                log.warn("Failed to create story-assigned notification: {}", e.getMessage());
            }
        }
        // Notify if already approved
        if (saved.isIs_approved()) {
            notificationService.notifyStoryCompleted(saved);
            try {
                if (saved.getAssigned_to() != null) {
                    notificationDatabaseService.createNotification(
                        saved.getAssigned_to().getId(),
                        "Story Completed",
                        "Your story '" + saved.getTitle() + "' has been completed",
                        Notification.NotificationType.STORY_COMPLETED,
                        "STORY",
                        saved.getStoryId()
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to create story-completed notification: {}", e.getMessage());
            }
        }
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "stories", allEntries = true)
    public Story updateFromDTO(Long id, StoryDTO dto) {
        Story existing = findById(id);
        if (existing == null) throw new IllegalArgumentException("Task not found with id: " + id);
        boolean wasAssigned = existing.getAssigned_to() != null;
        if (dto.getTitle() != null) existing.setTitle(dto.getTitle());
        if (dto.getDescription() != null) existing.setDeliverables(dto.getDescription());
        if (dto.getDueDate() != null) existing.setDueDate(dto.getDueDate());
        // map optional: deadline
        try { if (dto.getDeadline() != null) existing.setDeadline(dto.getDeadline()); } catch (Exception e) { }
        if (dto.getProjectId() != null) {
            Project p = projectRepo.findById(dto.getProjectId()).orElse(null);
            existing.setProjectId(p);
        }
        if (dto.getWorkflowStateId() != null) {
            Epic ws = stateRepo.findById(dto.getWorkflowStateId()).orElse(null);
            existing.setEpicId(ws);
        }
        if (dto.getAssignedToId() != null) {
            User u = userRepo.findById(dto.getAssignedToId()).orElse(null);
            existing.setAssigned_to(u);
            // notify newly assigned
            if (u != null && !wasAssigned && existing.isIs_approved()) notificationService.notifyStoryAssigned(existing);
        }
        if (dto.getCreatedById() != null) {
            User creator = userRepo.findById(dto.getCreatedById()).orElse(null);
            existing.setCreated_by(creator);
        }
        // if approved in DTO, notify manager
        if (dto.getIsApproved() != null && dto.getIsApproved()) {
            existing.setIs_approved(true);
            if (existing.getIs_end() == null) existing.setIs_end(java.time.LocalDate.now());
            notificationService.notifyStoryCompleted(existing);
        }
        return repo.save(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"stories", "epics", "projects"}, allEntries = true)
    public Story completeStory(Long id) {
        Story story = repo.findById(id).orElse(null);
        if (story == null) throw new IllegalArgumentException("Story not found with id: " + id);

        if (story.getIs_end() != null) {
            return story; // already completed
        }

        // Mark story complete
        story.setIs_approved(true);
        story.setIs_end(LocalDate.now());
        Story saved = repo.save(story);

        // Notify
        notificationService.notifyStoryCompleted(saved);
        try {
            if (saved.getAssigned_to() != null) {
                notificationDatabaseService.createNotification(
                    saved.getAssigned_to().getId(),
                    "Story Completed",
                    "Story '" + saved.getTitle() + "' has been marked as completed",
                    Notification.NotificationType.STORY_COMPLETED,
                    "STORY",
                    saved.getStoryId()
                );
            }
        } catch (Exception e) {
            log.warn("Failed to create story-completed notification: {}", e.getMessage());
        }

        // Cascade: check if all stories in this epic are now complete
        Epic epic = saved.getEpicId();
        if (epic != null) {
            cascadeEpicCompletion(epic);
        }

        return saved;
    }

    /**
     * Check if all stories under this epic are complete.
     * If so, mark the epic complete and cascade to the project.
     */
    private void cascadeEpicCompletion(Epic epic) {
        List<Story> epicStories = repo.findByEpicId(epic.getEpicId());
        boolean allComplete = !epicStories.isEmpty() && epicStories.stream()
                .allMatch(s -> s.getIs_end() != null);

        if (allComplete && epic.getIs_end() == null) {
            epic.setIs_approved(true);
            epic.setIs_end(LocalDate.now());
            stateRepo.save(epic);

            log.info("Epic '{}' auto-completed: all {} stories finished", epic.getName(), epicStories.size());

            notificationService.notifyEpicFinished(epic);
            try {
                User manager = epic.getManager_id();
                if (manager != null) {
                    notificationDatabaseService.createNotification(
                        manager.getId(),
                        "Epic Auto-Completed",
                        "Epic '" + epic.getName() + "' completed - all stories are done",
                        Notification.NotificationType.EPIC_COMPLETED,
                        "EPIC",
                        epic.getEpicId()
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to create epic-completed notification: {}", e.getMessage());
            }

            // Cascade: check if all epics in this project are now complete
            Project project = epic.getProjectId();
            if (project != null) {
                cascadeProjectCompletion(project);
            }
        }
    }

    /**
     * Check if all epics under this project are complete.
     * If so, mark the project complete.
     */
    private void cascadeProjectCompletion(Project project) {
        List<Epic> projectEpics = stateRepo.findByProjectId(project.getProjectId());
        boolean allComplete = !projectEpics.isEmpty() && projectEpics.stream()
                .allMatch(e -> e.getIs_end() != null);

        if (allComplete && project.getIs_end() == null) {
            project.setIs_approved(true);
            project.setIs_end(LocalDate.now());
            projectRepo.save(project);

            log.info("Project '{}' auto-completed: all {} epics finished", project.getName(), projectEpics.size());

            try {
                User manager = project.getManager_id();
                if (manager != null) {
                    notificationDatabaseService.createNotification(
                        manager.getId(),
                        "Project Auto-Completed",
                        "Project '" + project.getName() + "' completed - all epics are done",
                        Notification.NotificationType.PROJECT_COMPLETED,
                        "PROJECT",
                        project.getProjectId()
                    );
                }
            } catch (Exception e) {
                log.warn("Failed to create project-completed notification: {}", e.getMessage());
            }
        }
    }
}