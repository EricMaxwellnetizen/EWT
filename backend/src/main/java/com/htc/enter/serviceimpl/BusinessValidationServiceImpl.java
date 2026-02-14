package com.htc.enter.serviceimpl;

import com.htc.enter.model.*;
import com.htc.enter.repository.*;
import com.htc.enter.service.BusinessValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Business Validation Service Implementation for Elara
 * 
 * Implements comprehensive business rules and validation logic
 * Ensures data integrity, security, and business constraints
 */
@Service
@Transactional(readOnly = true)
public class BusinessValidationServiceImpl implements BusinessValidationService {
    
    private static final Logger log = LoggerFactory.getLogger(BusinessValidationServiceImpl.class);
    
    // Email validation pattern for @htcinc.com domain
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@htcinc\\.com$");
    
    // Business constants
    private static final int MIN_PROJECT_DURATION_DAYS = 1;
    private static final int MAX_PROJECT_DURATION_DAYS = 730; // 2 years
    private static final double MAX_STORY_HOURS = 160; // 20 working days
    private static final double MAX_DAILY_HOURS = 24;
    private static final int SLA_WARNING_DAYS = 3;
    
    private final ProjectRepository projectRepository;
    private final StoryRepository storyRepository;
    private final EpicRepository epicRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final SlaRuleRepository slaRuleRepository;
    
    public BusinessValidationServiceImpl(
            ProjectRepository projectRepository,
            StoryRepository storyRepository,
            EpicRepository epicRepository,
            UserRepository userRepository,
            ClientRepository clientRepository,
            SlaRuleRepository slaRuleRepository) {
        this.projectRepository = projectRepository;
        this.storyRepository = storyRepository;
        this.epicRepository = epicRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.slaRuleRepository = slaRuleRepository;
    }
    
    // ==================== PROJECT VALIDATION ====================
    
    @Override
    public void validateProjectCreation(Project project) {
        log.info("Validating project creation: {}", project.getName());
        
        // Validate project name
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        
        if (project.getName().length() < 3) {
            throw new IllegalArgumentException("Project name must be at least 3 characters long");
        }
        
        if (project.getName().length() > 200) {
            throw new IllegalArgumentException("Project name must not exceed 200 characters");
        }
        
        // Check for duplicate project names for the same client
        if (project.getClient_id() != null) {
            boolean duplicateExists = projectRepository.findAll().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(project.getName())
                    && p.getClient_id() != null
                    && p.getClient_id().getClient_id() == project.getClient_id().getClient_id());

            if (duplicateExists) {
                throw new IllegalArgumentException("A project with this name already exists for this client");
            }
        }
        
        // Validate deadline
        validateProjectDeadline(project.getDeadline());
        
        // Validate client exists
        if (project.getClient_id() != null) {
            Client client = clientRepository.findById(project.getClient_id().getClient_id()).orElse(null);
            if (client == null) {
                throw new IllegalArgumentException("Invalid client specified");
            }
        }
        
        // Validate project manager exists
        if (project.getManager_id() != null) {
            User manager = userRepository.findById(project.getManager_id().getId()).orElse(null);
            if (manager == null) {
                throw new IllegalArgumentException("Invalid project manager specified");
            }

            // Check if user has manager role (access level 2 or higher)
            if (manager.getAccessLevel() < 2) {
                throw new IllegalArgumentException("Selected user does not have manager privileges");
            }
        }
        
        log.info("Project validation passed: {}", project.getName());
    }
    
    @Override
    public void validateProjectUpdate(Project project, Project existingProject) {
        log.info("Validating project update: {}", project.getName());
        
        // Perform creation validations
        validateProjectCreation(project);
        
        // Additional update-specific validations
        if (existingProject.isIs_approved() && !project.isIs_approved()) {
            throw new IllegalArgumentException("Cannot unapprove a completed project");
        }
        
        // If project is approved, cannot change critical fields
        if (existingProject.isIs_approved()) {
            if (project.getClient_id() != null && existingProject.getClient_id() != null
                && project.getClient_id().getClient_id() != existingProject.getClient_id().getClient_id()) {
                throw new IllegalArgumentException("Cannot change client of an approved project");
            }
        }
        
        log.info("Project update validation passed: {}", project.getName());
    }
    
    @Override
    public void validateProjectDeadline(LocalDate deadline) {
        if (deadline == null) {
            throw new IllegalArgumentException("Project deadline is required");
        }
        
        LocalDate today = LocalDate.now();
        long daysUntilDeadline = ChronoUnit.DAYS.between(today, deadline);
        
        if (daysUntilDeadline < MIN_PROJECT_DURATION_DAYS) {
            throw new IllegalArgumentException("Project deadline must be at least " + MIN_PROJECT_DURATION_DAYS + " day in the future");
        }
        
        if (daysUntilDeadline > MAX_PROJECT_DURATION_DAYS) {
            throw new IllegalArgumentException("Project deadline cannot exceed " + MAX_PROJECT_DURATION_DAYS + " days from today");
        }
    }
    
    @Override
    public boolean canApproveProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        
        if (project == null || user == null) {
            return false;
        }
        
        // Only managers (level 2) and admins (level 3) can approve projects
        if (user.getAccessLevel() < 2) {
            return false;
        }
        
        // All stories in the project must be approved
        List<Story> stories = storyRepository.findAll().stream()
            .filter(s -> s.getProjectId() != null && s.getProjectId().getProjectId() == projectId)
            .collect(Collectors.toList());

        return stories.stream().allMatch(Story::isIs_approved);
    }
    
    @Override
    public boolean canDeleteProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        
        if (project == null || user == null) {
            return false;
        }
        
        // Cannot delete approved projects
        if (project.isIs_approved()) {
            log.warn("Cannot delete approved project: {}", projectId);
            return false;
        }
        
        // Only admins (level 3) or project managers can delete
    return user.getAccessLevel() == 3 ||
           (project.getManager_id() != null &&
        Objects.equals(project.getManager_id().getId(), userId));
    }
    
    // ==================== STORY VALIDATION ====================
    
    @Override
    public void validateStoryCreation(Story story) {
    log.info("Validating story creation: {}", story.getTitle());
        
        // Validate title
        if (story.getTitle() == null || story.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Story title is required");
        }
        
        if (story.getTitle().length() < 5) {
            throw new IllegalArgumentException("Story title must be at least 5 characters long");
        }
        
        if (story.getTitle().length() > 255) {
            throw new IllegalArgumentException("Story title must not exceed 255 characters");
        }
        
        // Validate deliverables/description
        if (story.getDeliverables() != null && story.getDeliverables().length() > 5000) {
            throw new IllegalArgumentException("Story description must not exceed 5000 characters");
        }
        
        // Validate due date
        if (story.getDueDate() == null) {
            throw new IllegalArgumentException("Story due date is required");
        }
        
        if (story.getDueDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Story due date cannot be in the past");
        }
        
        // Validate project association
        if (story.getProjectId() == null) {
            throw new IllegalArgumentException("Story must be associated with a project");
        }

        Project project = projectRepository.findById(story.getProjectId().getProjectId()).orElse(null);
        if (project == null) {
            throw new IllegalArgumentException("Invalid project specified for story");
        }
        
        // Story due date cannot exceed project deadline
        if (story.getDueDate().isAfter(project.getDeadline())) {
            throw new IllegalArgumentException("Story due date cannot exceed project deadline");
        }
        
    // Validate hours (estimated/actual may be null if not tracked on Story)
    validateStoryHours(story.getEstimatedHours(), story.getActualHours());
        
        // Validate assignee
        if (story.getAssigned_to() != null) {
            validateStoryAssignment(story, story.getAssigned_to());
        }
        
        log.info("Story validation passed: {}", story.getTitle());
    }
    
    @Override
    public void validateStoryUpdate(Story story, Story existingStory) {
        log.info("Validating story update: {}", story.getTitle());
        
        // Perform creation validations
        validateStoryCreation(story);
        
        // Cannot unapprove a story
        if (existingStory.isIs_approved() && !story.isIs_approved()) {
            throw new IllegalArgumentException("Cannot unapprove a completed story");
        }
        
        // If story is approved, actual hours must be logged
        if (story.isIs_approved() && (story.getActualHours() == null || story.getActualHours() == 0)) {
            throw new IllegalArgumentException("Approved story must have actual hours logged");
        }
        
        log.info("Story update validation passed: {}", story.getTitle());
    }
    
    @Override
    public void validateStoryAssignment(Story story, User assignee) {
        if (assignee == null) {
            return; // Assignment is optional
        }
        
        User user = userRepository.findById(assignee.getId()).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("Invalid user assigned to story");
        }
        
        // Check user workload
        Double currentWorkload = calculateTeamWorkload(assignee.getId());
        if (currentWorkload > 160) { // More than 4 weeks of work
            log.warn("User {} has high workload: {} hours", assignee.getId(), currentWorkload);
            throw new IllegalArgumentException("Assigned user has excessive workload. Please reassign or adjust deadlines.");
        }
    }
    
    @Override
    public boolean canApproveStory(Long storyId, Long userId) {
        Story story = storyRepository.findById(storyId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        
        if (story == null || user == null) {
            return false;
        }
        
        // Story must have actual hours logged
        if (story.getActualHours() == null || story.getActualHours() == 0) {
            return false;
        }
        
     // Only managers and admins can approve, or the story assignee
     return user.getAccessLevel() >= 2 ||
         (story.getAssigned_to() != null && Objects.equals(story.getAssigned_to().getId(), userId));
    }
    
    @Override
    public void validateStoryHours(Double estimatedHours, Double actualHours) {
        if (estimatedHours != null) {
            if (estimatedHours < 0) {
                throw new IllegalArgumentException("Estimated hours cannot be negative");
            }
            if (estimatedHours > MAX_STORY_HOURS) {
                throw new IllegalArgumentException("Estimated hours cannot exceed " + MAX_STORY_HOURS + " hours");
            }
        }
        
        if (actualHours != null) {
            if (actualHours < 0) {
                throw new IllegalArgumentException("Actual hours cannot be negative");
            }
            if (actualHours > MAX_STORY_HOURS) {
                throw new IllegalArgumentException("Actual hours cannot exceed " + MAX_STORY_HOURS + " hours");
            }
        }
        
        // If both are provided, actual should not be more than 3x estimated
        if (estimatedHours != null && actualHours != null && estimatedHours > 0) {
            if (actualHours > estimatedHours * 3) {
                log.warn("Actual hours ({}) significantly exceeds estimated hours ({})", actualHours, estimatedHours);
            }
        }
    }
    
    // ==================== EPIC VALIDATION ====================
    
    @Override
    public void validateEpicCreation(Epic epic) {
    log.info("Validating epic creation: {}", epic.getName());
        
        if (epic.getName() == null || epic.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Epic name is required");
        }
        
        if (epic.getName().length() < 3) {
            throw new IllegalArgumentException("Epic name must be at least 3 characters long");
        }
        
        if (epic.getDeliverables() != null && epic.getDeliverables().length() > 5000) {
            throw new IllegalArgumentException("Epic description must not exceed 5000 characters");
        }
        
        // Validate project association
        if (epic.getProjectId() != null) {
            Project project = projectRepository.findById(epic.getProjectId().getProjectId()).orElse(null);
            if (project == null) {
                throw new IllegalArgumentException("Invalid project specified for epic");
            }
        }
        
        log.info("Epic validation passed: {}", epic.getName());
    }
    
    @Override
    public void validateEpicUpdate(Epic epic, Epic existingEpic) {
        validateEpicCreation(epic);
        
        // If epic has approved stories, it should be marked as complete
        List<Story> epicStories = storyRepository.findAll().stream()
            .filter(s -> s.getEpicId() != null && s.getEpicId().getEpicId() == epic.getEpicId())
            .collect(Collectors.toList());

        if (!epicStories.isEmpty()) {
            long approvedCount = epicStories.stream().filter(Story::isIs_approved).count();
            if (approvedCount == epicStories.size()) {
                log.info("All stories in epic {} are approved", epic.getName());
            }
        }
    }
    
    @Override
    public boolean canCloseEpic(Long epicId) {
        List<Story> epicStories = storyRepository.findAll().stream()
            .filter(s -> s.getEpicId() != null && s.getEpicId().getEpicId() == epicId)
            .collect(Collectors.toList());

        // Can close if all stories are approved
        return epicStories.stream().allMatch(Story::isIs_approved);
    }
    
    // ==================== ACCESS CONTROL ====================
    
    @Override
    public boolean hasAccessToProject(Long userId, Long projectId) {
        User user = userRepository.findById(userId).orElse(null);
        Project project = projectRepository.findById(projectId).orElse(null);
        
        if (user == null || project == null) {
            return false;
        }
        
        // Admins have access to all projects
        if (user.getAccessLevel() == 3) {
            return true;
        }
        
        // Project manager has access
        if (project.getManager_id() != null &&
            Objects.equals(project.getManager_id().getId(), userId)) {
            return true;
        }
        
        // Check if user is assigned to any story in the project
        return storyRepository.findAll().stream()
            .anyMatch(s -> s.getProjectId() != null &&
                          s.getProjectId().getProjectId() == projectId &&
                          s.getAssigned_to() != null &&
                          Objects.equals(s.getAssigned_to().getId(), userId));
    }
    
    @Override
    public boolean hasAccessToStory(Long userId, Long storyId) {
        User user = userRepository.findById(userId).orElse(null);
        Story story = storyRepository.findById(storyId).orElse(null);
        
        if (user == null || story == null) {
            return false;
        }
        
        // Admins and managers have access
        if (user.getAccessLevel() >= 2) {
            return true;
        }
        
        // Assignee has access
        return story.getAssigned_to() != null && Objects.equals(story.getAssigned_to().getId(), userId);
    }
    
    @Override
    public boolean hasAccessToEpic(Long userId, Long epicId) {
        User user = userRepository.findById(userId).orElse(null);
        
        if (user == null) {
            return false;
        }
        
        // Admins and managers have access
        return user.getAccessLevel() >= 2;
    }
    
    @Override
    public boolean canManageUsers(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null && user.getAccessLevel() == 3; // Only admins
    }
    
    @Override
    public boolean canManageClients(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null && user.getAccessLevel() >= 2; // Managers and admins
    }
    
    // ==================== CLIENT VALIDATION ====================
    
    @Override
    public void validateClientCreation(Client client) {
        log.info("Validating client creation: {}", client.getName());
        
        if (client.getName() == null || client.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Client name is required");
        }
        
        if (client.getName().length() < 2) {
            throw new IllegalArgumentException("Client name must be at least 2 characters long");
        }
        
        // Check for duplicate client names
        boolean duplicateExists = clientRepository.findAll().stream()
            .anyMatch(c -> c.getName().equalsIgnoreCase(client.getName()));
        
        if (duplicateExists) {
            throw new IllegalArgumentException("A client with this name already exists");
        }
        
        // Validate email if provided
        if (client.getEmail() != null && !client.getEmail().trim().isEmpty()) {
            validateEmail(client.getEmail());
        }
        
        log.info("Client validation passed: {}", client.getName());
    }
    
    @Override
    public void validateClientUpdate(Client client, Client existingClient) {
        validateClientCreation(client);
    }
    
    @Override
    public boolean canDeleteClient(Long clientId) {
        // Cannot delete client if they have projects
        boolean hasProjects = projectRepository.findAll().stream()
            .anyMatch(p -> p.getClient_id() != null && p.getClient_id().getClient_id() == clientId);
        
        return !hasProjects;
    }
    
    // ==================== SLA VALIDATION ====================
    
    @Override
    public void validateSlaRule(SlaRule slaRule) {
        // Validate using fields available on SlaRule model
        if (slaRule.getStartPoint() == null || slaRule.getStartPoint().trim().isEmpty()) {
            throw new IllegalArgumentException("SLA rule start point is required");
        }

        if (slaRule.getDurationHours() < 0) {
            throw new IllegalArgumentException("Duration hours cannot be negative");
        }

        if (slaRule.getEscalationDelayHours() < 0) {
            throw new IllegalArgumentException("Escalation delay cannot be negative");
        }
    }
    
    @Override
    public boolean isSlaViolated(Story story) {
        if (story.getDueDate() == null) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        
        // Check if overdue
        if (today.isAfter(story.getDueDate()) && !story.isIs_approved()) {
            return true;
        }
        
        // Check if within warning period
        long daysUntilDue = ChronoUnit.DAYS.between(today, story.getDueDate());
        return daysUntilDue <= SLA_WARNING_DAYS && !story.isIs_approved();
    }
    
    @Override
    public Map<String, Object> calculateSlaMetrics(Long projectId) {
        List<Story> projectStories = storyRepository.findAll().stream()
            .filter(s -> s.getProjectId() != null && s.getProjectId().getProjectId() == projectId)
            .collect(Collectors.toList());

        long totalStories = projectStories.size();
        long completedStories = projectStories.stream().filter(Story::isIs_approved).count();
        long overdueStories = projectStories.stream()
            .filter(s -> !s.isIs_approved() && s.getDueDate().isBefore(LocalDate.now()))
            .count();
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalStories", totalStories);
        metrics.put("completedStories", completedStories);
        metrics.put("overdueStories", overdueStories);
        metrics.put("completionRate", totalStories > 0 ? (completedStories * 100.0 / totalStories) : 0);
        metrics.put("slaCompliance", totalStories > 0 ? ((totalStories - overdueStories) * 100.0 / totalStories) : 100);
        
        return metrics;
    }
    
    // ==================== TIME TRACKING ====================
    
    @Override
    public void validateTimeLog(Double hours, LocalDate date) {
        if (hours == null || hours <= 0) {
            throw new IllegalArgumentException("Hours must be greater than zero");
        }
        
        if (hours > MAX_DAILY_HOURS) {
            throw new IllegalArgumentException("Cannot log more than " + MAX_DAILY_HOURS + " hours in a day");
        }
        
        if (date == null) {
            throw new IllegalArgumentException("Date is required for time log");
        }
        
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot log time for future dates");
        }
        
        // Cannot log time more than 90 days in the past
        long daysInPast = ChronoUnit.DAYS.between(date, LocalDate.now());
        if (daysInPast > 90) {
            throw new IllegalArgumentException("Cannot log time more than 90 days in the past");
        }
    }
    
    @Override
    public boolean canLogTime(Long userId, Long storyId) {
        return hasAccessToStory(userId, storyId);
    }
    
    @Override
    public Double calculateTotalHours(Long storyId) {
        Story story = storyRepository.findById(storyId).orElse(null);
        return story != null && story.getActualHours() != null ? story.getActualHours() : 0.0;
    }
    
    // ==================== EMAIL VALIDATION ====================
    
    @Override
    public void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        if (!isValidEmailDomain(email)) {
            throw new IllegalArgumentException("Email must be a valid @htcinc.com address");
        }
        
        // Check for duplicate emails
        boolean duplicateExists = userRepository.findAll().stream()
            .anyMatch(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email));
        
        if (duplicateExists) {
            throw new IllegalArgumentException("This email address is already registered");
        }
    }
    
    @Override
    public boolean isValidEmailDomain(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    // ==================== BUSINESS LOGIC CHECKS ====================
    
    @Override
    public boolean isProjectOverdue(Long projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null || project.isIs_approved()) {
            return false;
        }
        return project.getDeadline().isBefore(LocalDate.now());
    }
    
    @Override
    public boolean isStoryOverdue(Long storyId) {
        Story story = storyRepository.findById(storyId).orElse(null);
        if (story == null || story.isIs_approved()) {
            return false;
        }
        return story.getDueDate().isBefore(LocalDate.now());
    }
    
    @Override
    public List<Project> getOverdueProjects() {
        return projectRepository.findAll().stream()
            .filter(p -> !p.isIs_approved() && p.getDeadline().isBefore(LocalDate.now()))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Story> getOverdueStories() {
        return storyRepository.findAll().stream()
            .filter(s -> !s.isIs_approved() && s.getDueDate().isBefore(LocalDate.now()))
            .collect(Collectors.toList());
    }
    
    @Override
    public Double calculateProjectProgress(Long projectId) {
        List<Story> projectStories = storyRepository.findAll().stream()
            .filter(s -> s.getProjectId() != null && s.getProjectId().getProjectId() == projectId)
            .collect(Collectors.toList());
        
        if (projectStories.isEmpty()) {
            return 0.0;
        }
        
        long completedStories = projectStories.stream().filter(Story::isIs_approved).count();
        return (completedStories * 100.0) / projectStories.size();
    }
    
    @Override
    public Double calculateTeamWorkload(Long userId) {
        return storyRepository.findAll().stream()
            .filter(s -> !s.isIs_approved() &&
                        s.getAssigned_to() != null &&
                        Objects.equals(s.getAssigned_to().getId(), userId))
            .mapToDouble(s -> s.getEstimatedHours() != null ? s.getEstimatedHours() : 0.0)
            .sum();
    }
    
    // ==================== DATA INTEGRITY ====================
    
    @Override
    public void validateDataConsistency(String entityType, Long entityId) {
        log.info("Validating data consistency for {} with ID: {}", entityType, entityId);
        
        switch (entityType.toLowerCase()) {
            case "project":
                Project project = projectRepository.findById(entityId).orElse(null);
                if (project != null && project.getClient_id() != null) {
                    if (clientRepository.findById(project.getClient_id().getClient_id()).isEmpty()) {
                        throw new IllegalStateException("Project references non-existent client");
                    }
                }
                break;
                
            case "story":
                Story story = storyRepository.findById(entityId).orElse(null);
                if (story != null && story.getProjectId() != null) {
                    if (projectRepository.findById(story.getProjectId().getProjectId()).isEmpty()) {
                        throw new IllegalStateException("Story references non-existent project");
                    }
                }
                break;
                
            default:
                log.warn("Unknown entity type for consistency check: {}", entityType);
        }
    }
    
    @Override
    public void checkCircularDependencies(Long epicId) {
        // Check for circular references in epic hierarchy
        Set<Long> visitedEpics = new HashSet<>();
        checkCircularDependenciesRecursive(epicId, visitedEpics);
    }
    
    private void checkCircularDependenciesRecursive(Long epicId, Set<Long> visitedEpics) {
        if (visitedEpics.contains(epicId)) {
            throw new IllegalStateException("Circular dependency detected in epic hierarchy");
        }
        
        visitedEpics.add(epicId);
        
        // Check related epics (if you have parent-child epic relationships)
        Epic epic = epicRepository.findById(epicId).orElse(null);
        if (epic != null && epic.getProjectId() != null) {
            List<Epic> relatedEpics = epicRepository.findAll().stream()
                .filter(e -> e.getProjectId() != null &&
                            e.getProjectId().getProjectId() == epic.getProjectId().getProjectId())
                .collect(Collectors.toList());
            
            for (Epic relatedEpic : relatedEpics) {
                if (!Objects.equals(Long.valueOf(relatedEpic.getEpicId()), epicId)) {
                    // Recursively check (if you implement parent-child relationships)
                }
            }
        }
        
        visitedEpics.remove(epicId);
    }
}
