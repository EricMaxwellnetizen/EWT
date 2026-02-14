/**
 * Request Payload Builders
 * These functions transform UI data into API-compatible formats
 */

import type { UserInput } from '../types';

/**
 * Builds a user creation payload from form data
 */
export const buildUserCreatePayload = (formData: UserInput): UserInput => {
  return {
    firstName: formData.firstName.trim(),
    lastName: formData.lastName.trim(),
    email: formData.email.toLowerCase().trim(),
    password: formData.password,
    phoneNumber: formData.phoneNumber?.trim() || '',
    username: formData.username.trim(),
    jobTitle: formData.jobTitle?.trim() || '',
    joiningDate: formData.joiningDate || new Date().toISOString().split('T')[0],
    reportingToId: formData.reportingToId || undefined,
    department: formData.department?.trim() || '',
    accessLevel: formData.accessLevel,
    role: formData.role,
    type: formData.type,
  };
};

/**
 * Builds a user update payload (excludes password)
 */
export const buildUserUpdatePayload = (formData: Partial<UserInput>): Partial<UserInput> => {
  const payload: Partial<UserInput> = {};
  
  if (formData.firstName) payload.firstName = formData.firstName.trim();
  if (formData.lastName) payload.lastName = formData.lastName.trim();
  if (formData.email) payload.email = formData.email.toLowerCase().trim();
  if (formData.phoneNumber !== undefined) payload.phoneNumber = formData.phoneNumber.trim();
  if (formData.username) payload.username = formData.username.trim();
  if (formData.jobTitle !== undefined) payload.jobTitle = formData.jobTitle.trim();
  if (formData.joiningDate) payload.joiningDate = formData.joiningDate;
  if (formData.reportingToId !== undefined) payload.reportingToId = formData.reportingToId;
  if (formData.department !== undefined) payload.department = formData.department.trim();
  if (formData.accessLevel !== undefined) payload.accessLevel = formData.accessLevel;
  if (formData.role) payload.role = formData.role;
  if (formData.type) payload.type = formData.type;
  
  return payload;
};
