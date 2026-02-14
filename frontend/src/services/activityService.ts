import { post } from '../lib/api';

export interface ActivityLog {
  type: 'create' | 'update' | 'delete';
  entityType: 'project' | 'epic' | 'story' | 'client' | 'sla' | 'user';
  entityId: string | number;
  entityName: string;
  description: string;
  timestamp: number;
}

export const activityService = {
  // Log activity and send to backend notification system
  logActivity: async (activity: ActivityLog): Promise<void> => {
    try {
      // Create notification in the backend
      await post('/notifications/create', {
        title: `${activity.type.charAt(0).toUpperCase() + activity.type.slice(1)} ${activity.entityType}`,
        message: activity.description,
        type: activity.type,
        relatedEntityType: activity.entityType,
        relatedEntityId: activity.entityId,
      });
    } catch (error) {
      console.error('Failed to log activity:', error);
      // Don't fail the operation if logging fails
    }
  },

  // Batch log multiple activities
  logActivities: async (activities: ActivityLog[]): Promise<void> => {
    try {
      const notifications = activities.map(activity => ({
        title: `${activity.type.charAt(0).toUpperCase() + activity.type.slice(1)} ${activity.entityType}`,
        message: activity.description,
        type: activity.type,
        relatedEntityType: activity.entityType,
        relatedEntityId: activity.entityId,
      }));

      await post('/notifications/create-batch', { notifications });
    } catch (error) {
      console.error('Failed to log activities:', error);
    }
  },
};
