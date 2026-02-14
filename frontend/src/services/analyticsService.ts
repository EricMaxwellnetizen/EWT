import { API_BASE_URL } from '../config/constants';
import api from '../lib/api';

/**
 * Analytics Service
 * Provides access to comprehensive business intelligence and analytics endpoints
 */

export interface DashboardMetrics {
  totalProjects: number;
  totalStories: number;
  totalEpics: number;
  totalClients: number;
  totalUsers: number;
  approvedStoriesCount: number;
  pendingStoriesCount: number;
  assignedStoriesCount: number;
  unassignedStoriesCount: number;
  overdueStoriesCount: number;
  usersByRole: Record<string, number>;
  activeUsersCount: number;
  inactiveUsersCount: number;
  totalEstimatedHours: number;
  totalActualHours: number;
  timeEfficiencyRatio: number;
}

export interface ProjectAnalytics {
  projectId: number;
  projectName: string;
  totalEpics: number;
  totalStories: number;
  completionPercentage: number;
}

export interface TeamAnalytics {
  usersByRole: Record<string, number>;
  activeUsersCount: number;
}

export interface UserAnalytics {
  userId: number;
  assignedStoriesCount: number;
  completedStoriesCount: number;
  pendingStoriesCount: number;
}

export interface WorkloadDistribution {
  [userId: string]: number;
}

export interface OverloadedUser {
  userId: number;
  username: string;
  pendingStoriesCount: number;
}

export interface UnderutilizedUser {
  userId: number;
  username: string;
  pendingStoriesCount: number;
}

export interface QualityMetrics {
  projectId: number;
  onTimeDeliveryRate: number;
  estimationAccuracy: number;
  totalEstimatedHours: number;
  totalActualHours: number;
}

export interface RiskAnalysis {
  projectId: number;
  highRiskCount: number;
  mediumRiskCount: number;
  lowRiskCount: number;
  highRiskStories: Array<{
    storyId: number;
    title: string;
    daysUntilDue: number;
  }>;
}

class AnalyticsService {
  /**
   * Get comprehensive dashboard metrics
   * Includes project counts, story status, user distribution, time tracking
   */
  async getDashboardMetrics(): Promise<DashboardMetrics> {
    const httpResponse = await api.get('/analytics/dashboard');
    return httpResponse.data;
  }

  /**
   * Get analytics for a specific project
   * @param projectId - The project ID
   */
  async getProjectAnalytics(projectId: number): Promise<ProjectAnalytics> {
    const httpResponse = await api.get(`/analytics/project/${projectId}`);
    return httpResponse.data;
  }

  /**
   * Get team-wide analytics
   * Requires MANAGER or ADMIN role
   */
  async getTeamAnalytics(): Promise<TeamAnalytics> {
    const httpResponse = await api.get('/analytics/team');
    return httpResponse.data;
  }

  /**
   * Get analytics for a specific user
   * @param userId - The user ID
   */
  async getUserAnalytics(userId: number): Promise<UserAnalytics> {
    const httpResponse = await api.get(`/analytics/user/${userId}`);
    return httpResponse.data;
  }

  /**
   * Get workload distribution across all users
   * Returns a map of userId -> story count
   * Requires MANAGER or ADMIN role
   */
  async getWorkloadDistribution(): Promise<WorkloadDistribution> {
    const httpResponse = await api.get('/analytics/workload');
    return httpResponse.data;
  }

  /**
   * Get list of overloaded users (users with too many pending stories)
   * @param threshold - Minimum number of pending stories to be considered overloaded (default: 10)
   * Requires MANAGER or ADMIN role
   */
  async getOverloadedUsers(threshold: number = 10): Promise<OverloadedUser[]> {
    const httpResponse = await api.get('/analytics/overloaded', {
      params: { threshold }
    });
    return httpResponse.data;
  }

  /**
   * Get list of underutilized users (users with too few pending stories)
   * @param threshold - Maximum number of pending stories to be considered underutilized (default: 3)
   * Requires MANAGER or ADMIN role
   */
  async getUnderutilizedUsers(threshold: number = 3): Promise<UnderutilizedUser[]> {
    const httpResponse = await api.get('/analytics/underutilized', {
      params: { threshold }
    });
    return httpResponse.data;
  }

  /**
   * Get quality metrics for a project
   * Includes on-time delivery rate and estimation accuracy
   * @param projectId - The project ID
   */
  async getQualityMetrics(projectId: number): Promise<QualityMetrics> {
    const httpResponse = await api.get(`/analytics/project/${projectId}/quality`);
    return httpResponse.data;
  }

  /**
   * Get risk analysis for a project
   * Categorizes stories as high/medium/low risk based on days until due
   * @param projectId - The project ID
   */
  async getRiskAnalysis(projectId: number): Promise<RiskAnalysis> {
    const httpResponse = await api.get(`/analytics/project/${projectId}/risks`);
    return httpResponse.data;
  }

  /**
   * Calculate on-time delivery rate for a project
   * @param projectId - The project ID
   * @returns Percentage of stories delivered by due date (0-100)
   */
  async calculateOnTimeDeliveryRate(projectId: number): Promise<number> {
    const httpResponse = await api.get(`/analytics/project/${projectId}/on-time-rate`);
    return httpResponse.data;
  }
}

export const analyticsService = new AnalyticsService();
export default analyticsService;
