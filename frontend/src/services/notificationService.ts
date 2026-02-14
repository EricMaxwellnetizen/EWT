import { get, put, del, post } from '../lib/api';
import { ENDPOINTS } from '../config/constants';

export interface Notification {
  id: number;
  title: string;
  message: string;
  type: string;
  isRead: boolean;
  relatedEntityType?: string;
  relatedEntityId?: number;
  createdAt: string;
  userId: number;
  userName: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
}

export const notificationService = {
  createNotification: (payload: {
    recipient?: string;
    subject?: string;
    message: string;
    priority?: string;
    templateId?: string;
  }): Promise<Notification> =>
    post(ENDPOINTS.NOTIFICATIONS_CREATE, payload),

  createNotificationsBatch: (payload: {
    notifications: Array<{
      recipient?: string;
      subject?: string;
      message: string;
      priority?: string;
      templateId?: string;
    }>;
  }): Promise<Notification[]> =>
    post(ENDPOINTS.NOTIFICATIONS_CREATE_BATCH, payload),

  getRecentNotifications: (): Promise<Notification[]> => 
    get(ENDPOINTS.NOTIFICATIONS_RECENT),

  getUnreadNotifications: (): Promise<Notification[]> => 
    get(ENDPOINTS.NOTIFICATIONS_UNREAD),

  getUnreadCount: (): Promise<{ unreadCount: number }> => 
    get(ENDPOINTS.NOTIFICATIONS_UNREAD_COUNT),

  getPaginatedNotifications: (params: { page: number; size: number }): Promise<PaginatedResponse<Notification>> => 
    get(ENDPOINTS.NOTIFICATIONS_PAGINATED, params),

  markAsRead: (id: number): Promise<Notification> => 
    put(ENDPOINTS.NOTIFICATIONS_MARK_READ(id), {}),

  markAllAsRead: (): Promise<{ message: string }> => 
    put(ENDPOINTS.NOTIFICATIONS_MARK_ALL_READ, {}),

  deleteNotification: (id: number): Promise<void> => 
    del(ENDPOINTS.NOTIFICATIONS_DELETE(id)),

  cleanupNotifications: (userId: number): Promise<{ message: string }> =>
    del(`${ENDPOINTS.NOTIFICATIONS_CLEANUP}?userId=${userId}`),
};
