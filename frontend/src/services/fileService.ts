import apiClient, { download } from '../lib/api';
import { ENDPOINTS } from '../config/constants';

export interface UploadedFileInfo {
  fileName: string;
  filePath?: string;
  fileDownloadUri: string;
  fileType?: string;
  size?: string;
  projectId?: string;
  epicId?: string;
  storyId?: string;
}

export interface FileListResponse {
  projectId?: number;
  epicId?: number;
  storyId?: number;
  files: string[];
  count: number;
}

export const fileService = {
  // Generic upload (backward compatibility)
  uploadSingle: async (file: File): Promise<UploadedFileInfo> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post(ENDPOINTS.FILE_UPLOAD, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  uploadMultiple: async (files: File[]): Promise<{ files: UploadedFileInfo[] }> => {
    const formData = new FormData();
    files.forEach((file) => formData.append('files', file));
    const response = await apiClient.post(ENDPOINTS.FILE_UPLOAD_MULTIPLE, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  // Entity-specific uploads
  uploadForProject: async (file: File, projectId: number): Promise<UploadedFileInfo> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post(`/files/upload/project/${projectId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  uploadForEpic: async (file: File, projectId: number, epicId: number): Promise<UploadedFileInfo> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post(`/files/upload/epic/${projectId}/${epicId}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  uploadForStory: async (
    file: File, 
    projectId: number, 
    storyId: number, 
    epicId?: number
  ): Promise<UploadedFileInfo> => {
    const formData = new FormData();
    formData.append('file', file);
    const url = epicId 
      ? `/files/upload/story/${projectId}/${storyId}?epicId=${epicId}`
      : `/files/upload/story/${projectId}/${storyId}`;
    const response = await apiClient.post(url, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  // List files by entity
  listFilesForProject: async (projectId: number): Promise<FileListResponse> => {
    const response = await apiClient.get(`/files/list/project/${projectId}`);
    return response.data;
  },

  listFilesForEpic: async (projectId: number, epicId: number): Promise<FileListResponse> => {
    const response = await apiClient.get(`/files/list/epic/${projectId}/${epicId}`);
    return response.data;
  },

  listFilesForStory: async (
    projectId: number, 
    storyId: number, 
    epicId?: number
  ): Promise<FileListResponse> => {
    const url = epicId 
      ? `/files/list/story/${projectId}/${storyId}?epicId=${epicId}`
      : `/files/list/story/${projectId}/${storyId}`;
    const response = await apiClient.get(url);
    return response.data;
  },

  // Download and delete
  downloadByName: async (fileName: string): Promise<void> => {
    return download(ENDPOINTS.FILE_DOWNLOAD(fileName), fileName);
  },

  downloadByPath: async (filePath: string, fileName: string): Promise<void> => {
    return download(`/files/download-path/${filePath}`, fileName);
  },

  deleteByName: async (fileName: string): Promise<{ message: string }> => {
    const response = await apiClient.delete(ENDPOINTS.FILE_DELETE(fileName));
    return response.data;
  },

  deleteByPath: async (filePath: string): Promise<{ message: string }> => {
    const response = await apiClient.delete(`/files/delete-path/${filePath}`);
    return response.data;
  },
};
