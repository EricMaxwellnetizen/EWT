import { ENDPOINTS } from '../config/constants';
import { getRoot, postRoot, putRoot, delRoot } from '../lib/api';

export interface TimeLog {
  id: number;
  storyId?: number;
  hoursWorked: number;
  description?: string;
  workDate: string;
  userName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface TimeLogRequest {
  storyId: number;
  hoursWorked: number;
  description?: string;
  workDate: string;
}

export const timeLogService = {
  logTime: (payload: TimeLogRequest): Promise<TimeLog> => postRoot(ENDPOINTS.TIME_LOGS, payload),

  getByStory: (storyId: number): Promise<TimeLog[]> => getRoot(ENDPOINTS.TIME_LOGS_BY_STORY(storyId)),

  getByUser: (userId: number): Promise<TimeLog[]> => getRoot(ENDPOINTS.TIME_LOGS_BY_USER(userId)),

  getTotalByStory: (storyId: number): Promise<number> => getRoot(ENDPOINTS.TIME_LOGS_TOTAL_BY_STORY(storyId)),

  getTotalByUser: (userId: number, params: { startDate: string; endDate: string }): Promise<number> =>
    getRoot(ENDPOINTS.TIME_LOGS_TOTAL_BY_USER(userId), params),

  update: (id: number, payload: TimeLogRequest): Promise<TimeLog> => putRoot(ENDPOINTS.TIME_LOGS_UPDATE(id), payload),

  delete: (id: number): Promise<void> => delRoot(ENDPOINTS.TIME_LOGS_DELETE(id)),
};
