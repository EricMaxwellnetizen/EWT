// User Types
export interface User {
  id?: number;
  username?: string;
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  phoneNumber?: string;
  accessLevel: number;
  role: string;
  department?: string;
  jobTitle?: string;
  joiningDate?: string;
  reportingToId?: number;
  reportingToUsername?: string;
  type?: 'admin' | 'manager' | 'employee' | 'user';
  reportingTo?: User;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserInput {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phoneNumber?: string;
  accessLevel: number;
  role: string;
  department?: string;
  username?: string;
  jobTitle?: string;
  joiningDate?: string;
  reportingToId?: number | null;
  type?: 'admin' | 'manager' | 'employee' | 'user';
  adminLevel?: number;
  accessScope?: string;
  approvalLimit?: number;
  managedWorkflowCount?: number;
  teamSize?: number;
  maxActiveTasks?: number;
  skillSet?: string[];
}

// Project Types
export interface Project {
  id?: number;
  name: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  clientId?: number;
  client?: Client;
  createdBy?: User;
  createdAt?: string;
  updatedAt?: string;
}

export interface ProjectDTO {
  name: string;
  description?: string;
  startDate?: string;
  endDate?: string;
  clientId?: number;
}

// Epic Types
export interface Epic {
  id?: number;
  name: string;
  description?: string;
  projectId: number;
  project?: Project;
  assignedToUserId?: number;
  assignedToUser?: User;
  estimatedStartDate?: string;
  estimatedEndDate?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface EpicDTO {
  name: string;
  description?: string;
  projectId: number;
  managerId: number;
  assignedToUserId?: number;
  estimatedStartDate?: string;
  estimatedEndDate?: string;
}

// Story Types
export interface Story {
  id?: number;
  name: string;
  description?: string;
  status?: string;
  priority?: string;
  projectId: number;
  project?: Project;
  epicId?: number;
  epic?: Epic;
  assignedToUserId?: number;
  assignedToUser?: User;
  estimatedStartDate?: string;
  estimatedEndDate?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface StoryDTO {
  name: string;
  description?: string;
  status?: string;
  priority?: string;
  projectId: number;
  epicId?: number;
  assignedToUserId?: number;
  estimatedStartDate?: string;
  estimatedEndDate?: string;
}

// Client Types
export interface Client {
  id?: number;
  clientId?: number;
  name: string;
  email: string;
  phoneNumber?: string;
  address?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ClientDTO {
  name: string;
  email: string;
  phoneNumber?: string;
  address?: string;
}

// SLA Rule Types
export interface SlaRule {
  id?: number;
  name: string;
  description?: string;
  targetEntityType: string;
  maxDurationDays: number;
  notificationThresholdDays: number;
  createdAt?: string;
  updatedAt?: string;
}

// Time Log Types
export interface TimeLog {
  id?: number;
  storyId: number;
  userId: number;
  date: string;
  hoursWorked: number;
  description?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface SlaRuleDTO {
  name: string;
  description?: string;
  targetEntityType: string;
  maxDurationDays: number;
  notificationThresholdDays: number;
}

// Auth Types
export interface LoginRequest {
  // Either `email` or `username` (or both) may be provided depending on backend
  email?: string;
  username?: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

// Pagination Types
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Dashboard Stats
export interface DashboardStats {
  totalProjects: number;
  activeProjects: number;
  completedProjects: number;
  totalStories: number;
  pendingStories: number;
  completedStories: number;
  totalUsers: number;
  slaBreaches: number;
}

// Legacy types for backward compatibility
export type ProjectFormData = ProjectDTO;
export type EpicFormData = EpicDTO;
export type StoryFormData = StoryDTO;
export type ClientFormData = ClientDTO;
export type SlaRuleFormData = SlaRuleDTO;
export type UserFormData = UserInput;
