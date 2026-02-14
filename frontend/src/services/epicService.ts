import { get, post, put, del, download } from '../lib/api';
import { ENDPOINTS } from '../config/constants';
import type { Epic, EpicFormData } from '../types';

export const epicService = {
  getAll: (): Promise<Epic[]> => get(ENDPOINTS.EPICS),

  getById: (id: number): Promise<Epic> => get(ENDPOINTS.EPIC_BY_ID(id)),

  create: (data: EpicFormData): Promise<Epic> => post(ENDPOINTS.EPIC_CREATE, data),

  update: (id: number, data: Partial<EpicFormData>): Promise<Epic> => 
    put(ENDPOINTS.EPIC_UPDATE(id), data),

  delete: (id: number): Promise<void> => del(ENDPOINTS.EPIC_DELETE(id)),

  downloadManagerEpics: (managerId: number): Promise<void> => 
    download(ENDPOINTS.EPIC_DOWNLOAD(managerId), `manager-${managerId}-epics.docx`),
};
