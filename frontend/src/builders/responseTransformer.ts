/**
 * Response Data Transformers
 * These functions transform API responses into UI-friendly formats
 */

import type { User, Project, Story, Epic, Client } from '../types';

/**
 * Normalizes user data from API response
 */
export const normalizeUser = (apiUser: any): User => {
  return {
    id: apiUser.id || apiUser.userId,
    firstName: apiUser.firstName || apiUser.first_name || '',
    lastName: apiUser.lastName || apiUser.last_name || '',
    email: apiUser.email || '',
    username: apiUser.username || apiUser.userName || '',
    phoneNumber: apiUser.phoneNumber || apiUser.phone_number || '',
    jobTitle: apiUser.jobTitle || apiUser.job_title || '',
    department: apiUser.department || '',
    accessLevel: apiUser.accessLevel || apiUser.access_level || 1,
    role: apiUser.role || 'USER',
    joiningDate: apiUser.joiningDate || apiUser.joining_date || '',
    reportingToId: apiUser.reportingToId || apiUser.reporting_to_id || null,
    isActive: apiUser.isActive ?? apiUser.is_active ?? true,
    createdAt: apiUser.createdAt || apiUser.created_at || '',
    updatedAt: apiUser.updatedAt || apiUser.updated_at || '',
  };
};

/**
 * Normalizes project data from API response
 */
export const normalizeProject = (apiProject: any): Project => {
  return {
    id: apiProject.id || apiProject.projectId,
    name: apiProject.name || '',
    description: apiProject.description || '',
    clientId: apiProject.clientId || apiProject.client_id,
    managerId: apiProject.managerId || apiProject.manager_id,
    startDate: apiProject.startDate || apiProject.start_date,
    endDate: apiProject.endDate || apiProject.end_date,
    status: apiProject.status || 'PLANNING',
    budget: apiProject.budget,
    client: apiProject.client,
    manager: apiProject.manager,
    createdAt: apiProject.createdAt || apiProject.created_at,
  };
};

/**
 * Normalizes story data from API response
 */
export const normalizeStory = (apiStory: any): Story => {
  return {
    id: apiStory.id || apiStory.storyId || apiStory.StoryId,
    name: apiStory.name || apiStory.title || '',
    description: apiStory.description || apiStory.deliverables || '',
    projectId: apiStory.projectId || apiStory.project_id,
    epicId: apiStory.epicId || apiStory.epic_id || apiStory.EpicId || apiStory.workflowStateId,
    assignedToId: apiStory.assignedToId || apiStory.assigned_to_id || apiStory.assignedTo,
    priority: apiStory.priority || 'MEDIUM',
    status: apiStory.status || 'TODO',
    estimatedHours: apiStory.estimatedHours || apiStory.estimated_hours,
    actualHours: apiStory.actualHours || apiStory.actual_hours,
    createdAt: apiStory.createdAt || apiStory.created_at,
  };
};

/**
 * Normalizes epic data from API response
 */
export const normalizeEpic = (apiEpic: any): Epic => {
  return {
    id: apiEpic.id || apiEpic.epicId || apiEpic.EpicId,
    name: apiEpic.name || '',
    description: apiEpic.description || apiEpic.deliverables || '',
    projectId: apiEpic.projectId || apiEpic.project_id,
    managerId: apiEpic.managerId || apiEpic.manager_id,
    assignedToUserId: apiEpic.assignedToUserId || apiEpic.assigned_to_user_id,
    estimatedStartDate: apiEpic.estimatedStartDate || apiEpic.estimated_start_date || apiEpic.isStart,
    estimatedEndDate: apiEpic.estimatedEndDate || apiEpic.estimated_end_date || apiEpic.isEnd,
    status: apiEpic.status || 'PLANNING',
    createdAt: apiEpic.createdAt || apiEpic.created_at,
  };
};

/**
 * Normalizes client data from API response
 */
export const normalizeClient = (apiClient: any): Client => {
  return {
    id: apiClient.id || apiClient.clientId,
    name: apiClient.name || '',
    email: apiClient.email || '',
    phoneNumber: apiClient.phoneNumber || apiClient.phone_number || '',
    address: apiClient.address || '',
    contactPerson: apiClient.contactPerson || apiClient.contact_person,
    industry: apiClient.industry,
    createdAt: apiClient.createdAt || apiClient.created_at,
  };
};
