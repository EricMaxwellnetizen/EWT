/**
 * Project Payload Builders
 */

import type { ProjectDTO } from '../types';

/**
 * Builds a project creation payload
 */
export const buildProjectCreatePayload = (formData: any): ProjectDTO => {
  return {
    name: formData.name.trim(),
    description: formData.description?.trim() || '',
    clientId: Number(formData.clientId),
    managerId: Number(formData.managerId),
    startDate: formData.startDate || null,
    endDate: formData.endDate || null,
    budget: formData.budget ? Number(formData.budget) : undefined,
    status: formData.status || 'PLANNING',
  };
};

/**
 * Builds a project update payload
 */
export const buildProjectUpdatePayload = (formData: Partial<any>): Partial<ProjectDTO> => {
  const payload: Partial<ProjectDTO> = {};
  
  if (formData.name) payload.name = formData.name.trim();
  if (formData.description !== undefined) payload.description = formData.description.trim();
  if (formData.clientId) payload.clientId = Number(formData.clientId);
  if (formData.managerId) payload.managerId = Number(formData.managerId);
  if (formData.startDate !== undefined) payload.startDate = formData.startDate;
  if (formData.endDate !== undefined) payload.endDate = formData.endDate;
  if (formData.budget !== undefined) payload.budget = Number(formData.budget);
  if (formData.status) payload.status = formData.status;
  
  return payload;
};
