import { get, post } from '../lib/api';
import { ENDPOINTS } from '../config/constants';
import type { LoginRequest, LoginResponse, User } from '../types';
import apiClient from '../lib/api';
import { userService } from './userService';

export const authService = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await post<LoginResponse>(ENDPOINTS.LOGIN, credentials);
    return response;
  },

  logout: async (): Promise<void> => {
    try {
      await post(ENDPOINTS.LOGOUT);
    } catch (error) {
      console.error('Logout error:', error);
    }
  },

  getCurrentUser: async (): Promise<User> => {
    return userService.getCurrentUser();
  },

  verifyToken: async (token: string): Promise<boolean> => {
    try {
      apiClient.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      await authService.getCurrentUser();
      return true;
    } catch {
      return false;
    }
  },
};
