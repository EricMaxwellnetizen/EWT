package com.htc.enter.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.htc.enter.service.AnalyticsService;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Get dashboard metrics
     * 
     * GET /api/v1/analytics/dashboard
     * 
     * Requires: Authentication
     * 
     * Returns: Overall system metrics for dashboard
     */
    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getDashboardMetrics() {
        Map<String, Object> metrics = analyticsService.getDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get project analytics
     * 
     * GET /api/v1/analytics/project/{projectId}
     * 
     * Requires: Authentication
     * 
     * Returns: Detailed analytics for specific project
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getProjectAnalytics(@PathVariable Long projectId) {
        Map<String, Object> analytics = analyticsService.getProjectAnalytics(projectId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get team analytics
     * 
     * GET /api/v1/analytics/team
     * 
     * Requires: Manager or Admin role
     * 
     * Returns: Team distribution and performance metrics
     */
    @GetMapping("/team")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getTeamAnalytics() {
        Map<String, Object> analytics = analyticsService.getTeamAnalytics();
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get user analytics
     * 
     * GET /api/v1/analytics/user/{userId}
     * 
     * Requires: Authentication
     * 
     * Returns: User-specific workload and performance metrics
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUserAnalytics(@PathVariable Long userId) {
        Map<String, Object> analytics = analyticsService.getUserAnalytics(userId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get workload distribution
     * 
     * GET /api/v1/analytics/workload
     * 
     * Requires: Manager or Admin role
     * 
     * Returns: How work is distributed across team members
     */
    @GetMapping("/workload")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getWorkloadDistribution() {
        Map<String, Object> distribution = analyticsService.getWorkloadDistribution();
        return ResponseEntity.ok(distribution);
    }

    /**
     * Get overloaded users
     * 
     * GET /api/v1/analytics/overloaded
     * 
     * Requires: Manager or Admin role
     * 
     * Returns: Users with story count above threshold
     */
    @GetMapping("/overloaded")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getOverloadedUsers(
            @RequestParam(defaultValue = "10") int threshold) {
        List<Map<String, Object>> overloaded = analyticsService.getOverloadedUsers(threshold);
        return ResponseEntity.ok(overloaded);
    }

    /**
     * Get underutilized users
     * 
     * GET /api/v1/analytics/underutilized
     * 
     * Requires: Manager or Admin role
     * 
     * Returns: Users with story count below threshold
     */
    @GetMapping("/underutilized")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getUnderutilizedUsers(
            @RequestParam(defaultValue = "3") int threshold) {
        List<Map<String, Object>> underutilized = analyticsService.getUnderutilizedUsers(threshold);
        return ResponseEntity.ok(underutilized);
    }

    /**
     * Get project quality metrics
     * 
     * GET /api/v1/analytics/project/{projectId}/quality
     * 
     * Requires: Authentication
     * 
     * Returns: Quality metrics including on-time delivery and estimation accuracy
     */
    @GetMapping("/project/{projectId}/quality")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getQualityMetrics(@PathVariable Long projectId) {
        Map<String, Object> metrics = analyticsService.getQualityMetrics(projectId);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Get project risk analysis
     * 
     * GET /api/v1/analytics/project/{projectId}/risks
     * 
     * Requires: Authentication
     * 
     * Returns: Risk assessment for project stories
     */
    @GetMapping("/project/{projectId}/risks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getRiskAnalysis(@PathVariable Long projectId) {
        Map<String, Object> risks = analyticsService.getRiskAnalysis(projectId);
        return ResponseEntity.ok(risks);
    }

    /**
     * Get on-time delivery rate
     * 
     * GET /api/v1/analytics/project/{projectId}/on-time-rate
     * 
     * Requires: Authentication
     * 
     * Returns: Percentage of stories delivered on or before due date
     */
    @GetMapping("/project/{projectId}/on-time-rate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Double> getOnTimeDeliveryRate(@PathVariable Long projectId) {
        Double rate = analyticsService.calculateOnTimeDeliveryRate(projectId);
        return ResponseEntity.ok(rate);
    }
}
