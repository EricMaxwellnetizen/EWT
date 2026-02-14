import axios, { AxiosError } from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { API_BASE_URL, STORAGE_KEYS } from '../config/constants';
import toast from 'react-hot-toast';
import { useAuthStore } from '../store/authStore';

// Utility to convert camelCase to snake_case
const camelToSnake = (str: string): string => {
  return str.replace(/[A-Z]/g, (letter) => `_${letter.toLowerCase()}`);
};

/**
 * Deep convert object keys from camelCase to snake_case
 * @param sourceObject - Object to convert
 * @returns Converted object with snake_case keys
 */
const convertKeysToSnakeCase = (sourceObject: any): any => {
  if (sourceObject === null || sourceObject === undefined) return sourceObject;
  if (typeof sourceObject !== 'object') return sourceObject;
  if (Array.isArray(sourceObject)) return sourceObject.map(convertKeysToSnakeCase);

  const convertedObject: Record<string, any> = {};
  for (const [camelCaseKey, keyValue] of Object.entries(sourceObject)) {
    const snakeCaseKey = camelToSnake(camelCaseKey);
    convertedObject[snakeCaseKey] = convertKeysToSnakeCase(keyValue);
  }
  return convertedObject;
};

// Create axios instance
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - Add auth token and convert to snake_case
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem(STORAGE_KEYS.TOKEN);
    if (token) {
      // Ensure headers object exists
      if (!config.headers) {
        config.headers = {} as any;
      }
      config.headers.Authorization = `Bearer ${token}`;
      console.log(`[API] ✓ Request WITH token: ${config.method?.toUpperCase()} ${config.url}`);
    } else {
      console.warn(`[API] ✗ Request WITHOUT token: ${config.method?.toUpperCase()} ${config.url}`);
      console.log('[API] localStorage state:', {
        hasToken: !!localStorage.getItem(STORAGE_KEYS.TOKEN),
        hasUser: !!localStorage.getItem(STORAGE_KEYS.USER),
        allKeys: Object.keys(localStorage)
      });
    }

    // Send request data as-is (backend uses camelCase)
    if ((config.method === 'post' || config.method === 'put') && config.data) {
      console.debug('Sending request data:', config.data);
    }

    return config;
  },
  (error: AxiosError) => {
    console.error('[API] Request interceptor error:', error);
    return Promise.reject(error);
  }
);

// Track if we've already handled a 401 to prevent cascade
let isHandling401 = false;

// Response interceptor - Handle errors globally
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    if (error.response) {
      const httpStatus = error.response.status;
      const errorResponseData = error.response.data as any;
      const requestUrl = error.config?.url || '';

      switch (httpStatus) {
        case 401:
          // NEVER treat login/logout 401 as a session expiry
          // A 401 on /auth/login just means wrong password
          if (requestUrl.includes('/auth/login') || requestUrl.includes('/auth/logout')) {
            // Let the login page handle its own error
            break;
          }
          
          // For all other endpoints, handle session expiry once
          if (!isHandling401) {
            isHandling401 = true;
            
            console.warn('[API] 401 on protected endpoint:', requestUrl);
            
            const { logout } = useAuthStore.getState();
            logout();
            
            // Reset the flag after cleanup
            setTimeout(() => {
              isHandling401 = false;
            }, 2000);
            
            // Redirect to login only if not already there
            const currentPath = window.location.pathname;
            if (currentPath !== '/login' && 
                currentPath !== '/' && 
                !currentPath.includes('/oauth2')) {
              window.location.href = '/login';
            }
          }
          break;
        
        case 403:
          toast.error(errorResponseData?.message || 'Access denied. Insufficient permissions.');
          break;
        
        case 404:
          console.debug('Resource not found:', error.config?.url);
          break;
        
        case 400:
          toast.error(errorResponseData?.message || 'Invalid request. Please check your input.');
          break;
        
        case 500:
          toast.error('Server error. Please try again later.');
          break;
        
        default:
          console.debug('API error:', httpStatus, errorResponseData?.message);
      }
    } else if (error.request) {
      console.warn('Network error - no response received');
    } else {
      console.warn('Error setting up request:', error.message);
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;

/**
 * Helper functions for common API operations
 * All functions return the response data directly (unwrapped from AxiosResponse)
 */

/**
 * Perform a GET request
 * @param url - API endpoint URL
 * @param params - Optional query parameters
 * @returns Promise with response data of type T
 */
export const get = async <T>(url: string, params?: any): Promise<T> => {
  const httpResponse = await apiClient.get<T>(url, { params });
  return httpResponse.data;
};

/**
 * Perform a POST request
 * @param url - API endpoint URL
 * @param requestBody - Data to send in request body
 * @returns Promise with response data of type T
 */
export const post = async <T>(url: string, requestBody?: any): Promise<T> => {
  const httpResponse = await apiClient.post<T>(url, requestBody);
  return httpResponse.data;
};

/**
 * Perform a PUT request
 * @param url - API endpoint URL
 * @param requestBody - Data to send in request body
 * @returns Promise with response data of type T
 */
export const put = async <T>(url: string, requestBody?: any): Promise<T> => {
  const httpResponse = await apiClient.put<T>(url, requestBody);
  return httpResponse.data;
};

/**
 * Perform a DELETE request
 * @param url - API endpoint URL
 * @returns Promise with response data of type T
 */
export const del = async <T>(url: string): Promise<T> => {
  const httpResponse = await apiClient.delete<T>(url);
  return httpResponse.data;
};

/**
 * Perform a PATCH request
 * @param url - API endpoint URL
 * @param requestBody - Data to send in request body
 * @returns Promise with response data of type T
 */
export const patch = async <T>(url: string, requestBody?: any): Promise<T> => {
  const httpResponse = await apiClient.patch<T>(url, requestBody);
  return httpResponse.data;
};

/**
 * Root-level API helpers for endpoints outside API_BASE_URL
 * Used for legacy endpoints like /api/timelogs that don't use /api/v1 prefix
 */

/**
 * Perform a GET request with root baseURL
 * @param url - Full API endpoint URL
 * @param params - Optional query parameters
 * @returns Promise with response data of type T
 */
export const getRoot = async <T>(url: string, params?: any): Promise<T> => {
  const httpResponse = await apiClient.get<T>(url, { params, baseURL: '' });
  return httpResponse.data;
};

/**
 * Perform a POST request with root baseURL
 * @param url - Full API endpoint URL
 * @param requestBody - Data to send in request body
 * @returns Promise with response data of type T
 */
export const postRoot = async <T>(url: string, requestBody?: any): Promise<T> => {
  const httpResponse = await apiClient.post<T>(url, requestBody, { baseURL: '' });
  return httpResponse.data;
};

/**
 * Perform a PUT request with root baseURL
 * @param url - Full API endpoint URL
 * @param requestBody - Data to send in request body
 * @returns Promise with response data of type T
 */
export const putRoot = async <T>(url: string, requestBody?: any): Promise<T> => {
  const httpResponse = await apiClient.put<T>(url, requestBody, { baseURL: '' });
  return httpResponse.data;
};

/**
 * Perform a DELETE request with root baseURL
 * @param url - Full API endpoint URL
 * @returns Promise with response data of type T
 */
export const delRoot = async <T>(url: string): Promise<T> => {
  const httpResponse = await apiClient.delete<T>(url, { baseURL: '' });
  return httpResponse.data;
};

/**
 * Download a file from the server
 * Triggers browser download of a blob response
 * @param url - API endpoint URL for the file
 * @param filename - Optional filename for the downloaded file
 */
export const download = async (url: string, filename?: string): Promise<void> => {
  const httpResponse = await apiClient.get(url, { responseType: 'blob' });
  const fileBlob = new Blob([httpResponse.data]);
  const objectUrl = window.URL.createObjectURL(fileBlob);
  const downloadLink = document.createElement('a');
  downloadLink.href = objectUrl;
  downloadLink.download = filename || 'download';
  document.body.appendChild(downloadLink);
  downloadLink.click();
  downloadLink.remove();
  window.URL.revokeObjectURL(objectUrl);
};
