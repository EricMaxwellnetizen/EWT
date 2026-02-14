package com.htc.enter.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Analytics Service for Elara
 * 
 * Provides business intelligence and analytical insights
 * Supports dashboard visualizations and reporting
 */
public interface AnalyticsService {
    
    /**
     * Dashboard metrics
     */
    Map<String, Object> getDashboardMetrics();
    Map<String, Object> getProjectAnalytics(Long projectId);
    Map<String, Object> getTeamAnalytics();
    Map<String, Object> getUserAnalytics(Long userId);
    
    /**
     * Time-based analytics
     */
    Map<String, Object> getMonthlyTrends(int months);
    Map<String, Object> getWeeklyActivity();
    List<Map<String, Object>> getProjectTimeline(Long projectId);
    
    /**
     * Performance metrics
     */
    Map<String, Object> getTeamPerformanceMetrics();
    Double calculateVelocity(Long projectId);
    Double calculateBurndownRate(Long projectId);
    Map<String, Object> getEstimationAccuracy(Long projectId);
    
    /**
     * Workload analytics
     */
    Map<String, Object> getWorkloadDistribution();
    Map<String, Object> getUserWorkload(Long userId);
    List<Map<String, Object>> getOverloadedUsers(int threshold);
    List<Map<String, Object>> getUnderutilizedUsers(int threshold);
    
    /**
     * Quality metrics
     */
    Double calculateOnTimeDeliveryRate(Long projectId);
    Map<String, Object> getQualityMetrics(Long projectId);
    List<Map<String, Object>> getHighRiskStories();
    Map<String, Object> getRiskAnalysis(Long projectId);
    
    /**
     * Client analytics
     */
    Map<String, Object> getClientMetrics(Long clientId);
    List<Map<String, Object>> getTopClients(int limit);
    Map<String, Object> getClientSatisfactionMetrics();
    
    /**
     * Resource planning
     */
    Map<String, Object> getResourceUtilization();
    List<Map<String, Object>> predictBottlenecks(int daysAhead);
    Map<String, Object> getCapacityPlanning();
    
    /**
     * Export and reporting
     */
    Map<String, Object> generateExecutiveReport(LocalDate startDate, LocalDate endDate);
    Map<String, Object> generateProjectReport(Long projectId);
    Map<String, Object> generateTeamReport();
}
