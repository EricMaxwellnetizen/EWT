package com.htc.enter.service;

import com.htc.enter.model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Business Validation Service for Elara
 * 
 * Centralized service for all business rules and validation logic
 * Ensures data integrity and business constraints across the application
 */
public interface BusinessValidationService {
    
    /**
     * Validate project business rules
     */
    void validateProjectCreation(Project project);
    void validateProjectUpdate(Project project, Project existingProject);
    void validateProjectDeadline(LocalDate deadline);
    boolean canApproveProject(Long projectId, Long userId);
    boolean canDeleteProject(Long projectId, Long userId);
    
    /**
     * Validate story business rules
     */
    void validateStoryCreation(Story story);
    void validateStoryUpdate(Story story, Story existingStory);
    void validateStoryAssignment(Story story, User assignee);
    boolean canApproveStory(Long storyId, Long userId);
    void validateStoryHours(Double estimatedHours, Double actualHours);
    
    /**
     * Validate epic business rules
     */
    void validateEpicCreation(Epic epic);
    void validateEpicUpdate(Epic epic, Epic existingEpic);
    boolean canCloseEpic(Long epicId);
    
    /**
     * Validate user access and permissions
     */
    boolean hasAccessToProject(Long userId, Long projectId);
    boolean hasAccessToStory(Long userId, Long storyId);
    boolean hasAccessToEpic(Long userId, Long epicId);
    boolean canManageUsers(Long userId);
    boolean canManageClients(Long userId);
    
    /**
     * Validate client business rules
     */
    void validateClientCreation(Client client);
    void validateClientUpdate(Client client, Client existingClient);
    boolean canDeleteClient(Long clientId);
    
    /**
     * Validate SLA rules
     */
    void validateSlaRule(SlaRule slaRule);
    boolean isSlaViolated(Story story);
    Map<String, Object> calculateSlaMetrics(Long projectId);
    
    /**
     * Validate time tracking
     */
    void validateTimeLog(Double hours, LocalDate date);
    boolean canLogTime(Long userId, Long storyId);
    Double calculateTotalHours(Long storyId);
    
    /**
     * Validate email constraints
     */
    void validateEmail(String email);
    boolean isValidEmailDomain(String email);
    
    /**
     * Business logic checks
     */
    boolean isProjectOverdue(Long projectId);
    boolean isStoryOverdue(Long storyId);
    List<Project> getOverdueProjects();
    List<Story> getOverdueStories();
    Double calculateProjectProgress(Long projectId);
    Double calculateTeamWorkload(Long userId);
    
    /**
     * Data integrity checks
     */
    void validateDataConsistency(String entityType, Long entityId);
    void checkCircularDependencies(Long epicId);
}
