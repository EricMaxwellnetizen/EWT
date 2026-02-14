import { get, post, put, del, download } from '../lib/api';
import { ENDPOINTS } from '../config/constants';
import type { User, PaginatedResponse, UserFormData } from '../types';

/**
 * Resolve user role from various API response formats
 * Handles multiple backend schemas for backward compatibility
 * 
 * @param rawUserData - Raw user data from API
 * @returns Normalized role string (ADMIN, MANAGER, EMPLOYEE, USER)
 */
const resolveRole = (rawUserData: any): string => {
  if (rawUserData?.role) {
    return String(rawUserData.role).toUpperCase();
  }
  if (rawUserData?.type) {
    const userType = String(rawUserData.type).toUpperCase();
    if (userType === 'ADMIN') return 'ADMIN';
    if (userType === 'MANAGER') return 'MANAGER';
    if (userType === 'EMPLOYEE') return 'EMPLOYEE';
  }
  // Fall back to deriving role from access level
  const accessLevel = Number(rawUserData?.accessLevel ?? rawUserData?.access_level ?? 1);
  if (accessLevel >= 5) return 'ADMIN';
  if (accessLevel >= 4) return 'SENIOR_MANAGER';
  if (accessLevel >= 3) return 'MANAGER';
  if (accessLevel >= 2) return 'EMPLOYEE';
  return 'USER';
};

/**
 * Split a full name into firstName and lastName
 * @param fullName - Complete name string
 * @returns Object with firstName and lastName
 */
const splitName = (fullName: string) => {
  const trimmedName = fullName.trim();
  if (!trimmedName) return { firstName: 'User', lastName: '' };
  const nameParts = trimmedName.split(' ');
  if (nameParts.length === 1) return { firstName: nameParts[0], lastName: '' };
  return { firstName: nameParts[0], lastName: nameParts.slice(1).join(' ') };
};

/**
 * Normalize user response from various API formats
 * Handles different field naming conventions and derives missing names
 * 
 * @param rawUserData - Raw user data from API
 * @returns Normalized User object
 */
export const normalizeUser = (rawUserData: any): User => {
  const username = rawUserData?.username || rawUserData?.userName || '';
  const jobTitle = rawUserData?.job_title || rawUserData?.jobTitle || '';
  const sourceNameForParsing = jobTitle || username;
  let firstName = 'User';
  let lastName = '';

  // Attempt to derive firstName/lastName if not directly provided
  if (sourceNameForParsing) {
    if (sourceNameForParsing.includes(' ')) {
      ({ firstName, lastName } = splitName(sourceNameForParsing));
    } else if (sourceNameForParsing.includes('.') || sourceNameForParsing.includes('_')) {
      const nameParts = sourceNameForParsing.split(/[._]/g);
      firstName = nameParts[0] || 'User';
      lastName = nameParts.slice(1).join(' ') || '';
    } else {
      firstName = sourceNameForParsing;
    }
  }

  return {
    id: rawUserData?.id ?? rawUserData?.userId,
    username,
    firstName,
    lastName,
    email: rawUserData?.email || '',
    phoneNumber: rawUserData?.phoneNumber,
    accessLevel: Number(rawUserData?.accessLevel ?? rawUserData?.access_level ?? 1),
    role: resolveRole(rawUserData),
    department: rawUserData?.department,
    jobTitle: rawUserData?.job_title || rawUserData?.jobTitle,
    joiningDate: rawUserData?.joining_date || rawUserData?.joiningDate,
    reportingToId: rawUserData?.reporting_to_id || rawUserData?.reportingToId,
    reportingToUsername: rawUserData?.reportingToUsername,
    type: rawUserData?.type,
    reportingTo: rawUserData?.reportingTo,
    createdAt: rawUserData?.createdAt,
    updatedAt: rawUserData?.updatedAt,
  };
};

export const userService = {
  /**
   * Fetch all users from the server
   * @returns Promise with array of normalized user objects
   */
  getAll: async (): Promise<User[]> => {
    const usersArray = await get<any[]>(ENDPOINTS.USERS);
    return Array.isArray(usersArray) ? usersArray.map(normalizeUser) : [];
  },

  /**
   * Fetch paginated users with optional sorting
   * @param params - Pagination and sorting parameters
   * @returns Promise with paginated response containing normalized users
   */
  getPaginated: async (params: { 
    page: number; 
    size: number; 
    sortBy?: string; 
    sortDirection?: string 
  }): Promise<PaginatedResponse<User>> => {
    const paginatedResponse = await get<PaginatedResponse<any>>(ENDPOINTS.USERS_PAGINATED, params);
    return {
      ...paginatedResponse,
      content: Array.isArray(paginatedResponse?.content) ? paginatedResponse.content.map(normalizeUser) : [],
    };
  },

  /**
   * Fetch a single user by ID
   * @param id - User ID
   * @returns Promise with normalized user object
   */
  getById: async (id: number): Promise<User> => 
    normalizeUser(await get(ENDPOINTS.USER_BY_ID(id))),

  /**
   * Fetch the currently authenticated user's profile
   * @returns Promise with normalized user object
   */
  getCurrentUser: async (): Promise<User> => 
    normalizeUser(await get(ENDPOINTS.USER_ME)),

  /**
   * Create a new user
   * @param userFormData - User creation data
   * @returns Promise with created user object
   */
  create: (userFormData: UserFormData): Promise<User> => 
    post(ENDPOINTS.USER_CREATE, userFormData),

  /**
   * Update an existing user
   * @param id - User ID
   * @param userUpdateData - Partial user data to update
   * @returns Promise with updated user object
   */
  update: (id: number, userUpdateData: Partial<UserFormData>): Promise<User> => 
    put(ENDPOINTS.USER_UPDATE(id), userUpdateData),

  /**
   * Update the currently authenticated user's profile
   * @param profileUpdateData - Partial user data to update
   * @returns Promise with updated user object
   */
  updateProfile: (profileUpdateData: Partial<UserFormData>): Promise<User> => 
    put(ENDPOINTS.USER_ME, profileUpdateData),

  /**
   * Delete a user
   * @param id - User ID
   * @returns Promise that resolves when delete is complete
   */
  delete: (id: number): Promise<void> => 
    del(ENDPOINTS.USER_DELETE(id)),

  /**
   * Change the current user's password
   * @param oldPassword - Current password for verification
   * @param newPassword - New password to set
   * @returns Promise with success message
   */
  changePassword: (oldPassword: string, newPassword: string): Promise<{ message: string }> => 
    put(ENDPOINTS.USER_CHANGE_PASSWORD, { oldPassword, newPassword }),

  /**
   * Upload profile picture for the current user
   * @param imageFile - Image file to upload
   * @returns Promise with file upload result
   */
  uploadProfilePicture: (imageFile: File): Promise<{ fileName: string; fileDownloadUri: string; message: string }> => {
    const formData = new FormData();
    formData.append('file', imageFile);
    return post('/user/me/profile-picture', formData);
  },

  /**
   * Get list of users that the current user can edit (based on access level)
   * @returns Promise with array of editable users
   */
  getEditableUsers: (): Promise<User[]> => 
    get(ENDPOINTS.USER_EDITABLE),

  /**
   * Check if the current user can edit a specific user
   * @param userId - User ID to check
   * @returns Promise with permission result and reason
   */
  canEdit: (userId: number): Promise<{ canEdit: boolean; reason: string }> => 
    get(ENDPOINTS.USER_CAN_EDIT(userId)),

  /**
   * Download users report as Word document
   * @returns Promise that resolves when download starts
   */
  downloadDocument: (): Promise<void> => 
    download(ENDPOINTS.USER_DOWNLOAD, 'users.docx'),
};
