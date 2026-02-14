import { get, post, put, del, download, patch } from '../lib/api';
import { ENDPOINTS } from '../config/constants';
import type { Story, StoryFormData } from '../types';

export const storyService = {
  getAll: (): Promise<Story[]> => get(ENDPOINTS.STORIES),

  getById: (id: number): Promise<Story> => get(ENDPOINTS.STORY_BY_ID(id)),

  create: (data: StoryFormData): Promise<Story> => post(ENDPOINTS.STORY_CREATE, data),

  update: (id: number, data: Partial<StoryFormData>): Promise<Story> => 
    put(ENDPOINTS.STORY_UPDATE(id), data),

  delete: (id: number): Promise<void> => del(ENDPOINTS.STORY_DELETE(id)),

  complete: (id: number): Promise<Story> => patch(ENDPOINTS.STORY_COMPLETE(id), {}),

  downloadManagerStories: (managerId: number): Promise<void> => 
    download(ENDPOINTS.STORY_DOWNLOAD(managerId), `manager-${managerId}-stories.docx`),
};
