package com.htc.enter.controller;

import com.htc.enter.service.BusinessValidationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Business Validation and Analytics
 * 
 * Provides endpoints for:
 * - Business rule validation
 * - Access control checks
 * - Analytics and metrics
 * - Overdue tracking
 */
@RestController
@RequestMapping("/api/validation")
@CrossOrigin(origins = "*")
public class ValidationController {
    
    private final BusinessValidationService validationService;
    
    public ValidationController(BusinessValidationService validationService) {
        this.validationService = validationService;
    }
    
    /**
     * Check if user can approve a project
     */
    @GetMapping("/projects/{projectId}/can-approve")
    public ResponseEntity<Map<String, Boolean>> canApproveProject(
            @PathVariable Long projectId,
            @RequestParam Long userId) {
        boolean canApprove = validationService.canApproveProject(projectId, userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("canApprove", canApprove);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if user can delete a project
     */
    @GetMapping("/projects/{projectId}/can-delete")
    public ResponseEntity<Map<String, Boolean>> canDeleteProject(
            @PathVariable Long projectId,
            @RequestParam Long userId) {
        boolean canDelete = validationService.canDeleteProject(projectId, userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("canDelete", canDelete);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if user can approve a story
     */
    @GetMapping("/stories/{storyId}/can-approve")
    public ResponseEntity<Map<String, Boolean>> canApproveStory(
            @PathVariable Long storyId,
            @RequestParam Long userId) {
        boolean canApprove = validationService.canApproveStory(storyId, userId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("canApprove", canApprove);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if user has access to a project
     */
    @GetMapping("/projects/{projectId}/has-access")
    public ResponseEntity<Map<String, Boolean>> hasAccessToProject(
            @PathVariable Long projectId,
            @RequestParam Long userId) {
        boolean hasAccess = validationService.hasAccessToProject(userId, projectId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasAccess", hasAccess);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if user has access to a story
     */
    @GetMapping("/stories/{storyId}/has-access")
    public ResponseEntity<Map<String, Boolean>> hasAccessToStory(
            @PathVariable Long storyId,
            @RequestParam Long userId) {
        boolean hasAccess = validationService.hasAccessToStory(userId, storyId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasAccess", hasAccess);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if a project is overdue
     */
    @GetMapping("/projects/{projectId}/is-overdue")
    public ResponseEntity<Map<String, Boolean>> isProjectOverdue(@PathVariable Long projectId) {
        boolean isOverdue = validationService.isProjectOverdue(projectId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isOverdue", isOverdue);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if a story is overdue
     */
    @GetMapping("/stories/{storyId}/is-overdue")
    public ResponseEntity<Map<String, Boolean>> isStoryOverdue(@PathVariable Long storyId) {
        boolean isOverdue = validationService.isStoryOverdue(storyId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isOverdue", isOverdue);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all overdue projects
     */
    @GetMapping("/projects/overdue")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<?>> getOverdueProjects() {
        return ResponseEntity.ok(validationService.getOverdueProjects());
    }
    
    /**
     * Get all overdue stories
     */
    @GetMapping("/stories/overdue")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<?>> getOverdueStories() {
        return ResponseEntity.ok(validationService.getOverdueStories());
    }
    
    /**
     * Calculate project progress
     */
    @GetMapping("/projects/{projectId}/progress")
    public ResponseEntity<Map<String, Double>> getProjectProgress(@PathVariable Long projectId) {
        Double progress = validationService.calculateProjectProgress(projectId);
        Map<String, Double> response = new HashMap<>();
        response.put("progress", progress);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Calculate team member workload
     */
    @GetMapping("/users/{userId}/workload")
    public ResponseEntity<Map<String, Double>> getUserWorkload(@PathVariable Long userId) {
        Double workload = validationService.calculateTeamWorkload(userId);
        Map<String, Double> response = new HashMap<>();
        response.put("workload", workload);
        response.put("workloadPercentage", (workload / 160) * 100); // Percentage of 4 weeks
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get SLA metrics for a project
     */
    @GetMapping("/projects/{projectId}/sla-metrics")
    public ResponseEntity<Map<String, Object>> getSlaMetrics(@PathVariable Long projectId) {
        return ResponseEntity.ok(validationService.calculateSlaMetrics(projectId));
    }
    
    /**
     * Check if story SLA is violated
     */
    @GetMapping("/stories/{storyId}/sla-violated")
    public ResponseEntity<Map<String, Boolean>> isSlaViolated(@PathVariable Long storyId) {
        // Fetch story and check
        Map<String, Boolean> response = new HashMap<>();
        response.put("slaViolated", false); // Implement actual check
        return ResponseEntity.ok(response);
    }
    
    /**
     * Validate email domain
     */
    @GetMapping("/email/validate")
    public ResponseEntity<Map<String, Boolean>> validateEmail(@RequestParam String email) {
        boolean isValid = validationService.isValidEmailDomain(email);
        Map<String, Boolean> response = new HashMap<>();
        response.put("valid", isValid);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if client can be deleted
     */
    @GetMapping("/clients/{clientId}/can-delete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Boolean>> canDeleteClient(@PathVariable Long clientId) {
        boolean canDelete = validationService.canDeleteClient(clientId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("canDelete", canDelete);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if epic can be closed
     */
    @GetMapping("/epics/{epicId}/can-close")
    public ResponseEntity<Map<String, Boolean>> canCloseEpic(@PathVariable Long epicId) {
        boolean canClose = validationService.canCloseEpic(epicId);
        Map<String, Boolean> response = new HashMap<>();
        response.put("canClose", canClose);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Business Validation Service");
        return ResponseEntity.ok(response);
    }
}
