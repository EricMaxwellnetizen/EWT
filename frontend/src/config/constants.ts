// API Base URL - change this to your backend URL
export const API_BASE_URL = '/api/v1';

// API Endpoints
export const ENDPOINTS = {
  // Auth
  LOGIN: '/auth/login',
  LOGOUT: '/auth/logout',
  
  // Users
  USERS: '/user/get',
  USERS_PAGINATED: '/user/paginated',
  USER_BY_ID: (id: number) => `/user/${id}`,
  USER_ME: '/user/me',
  USER_CREATE: '/user/create',
  USER_UPDATE: (id: number) => `/user/${id}`,
  USER_DELETE: (id: number) => `/user/${id}`,
  USER_DOWNLOAD: '/user/download',
  USER_CHANGE_PASSWORD: '/user/me/password',
  USER_EDITABLE: '/user/editable',
  USER_CAN_EDIT: (id: number) => `/user/can-edit/${id}`,
  
  // Projects
  PROJECTS: '/project/get',
  PROJECTS_PAGINATED: '/project/paginated',
  PROJECT_BY_ID: (id: number) => `/project/${id}`,
  PROJECT_CREATE: '/project/create',
  PROJECT_UPDATE: (id: number) => `/project/${id}`,
  PROJECT_DELETE: (id: number) => `/project/${id}`,
  PROJECT_DOWNLOAD: (managerId: number) => `/project/manager/${managerId}/download`,
  
  // Epics
  EPICS: '/epic/get',
  EPIC_BY_ID: (id: number) => `/epic/${id}`,
  EPIC_CREATE: '/epic/create',
  EPIC_UPDATE: (id: number) => `/epic/${id}`,
  EPIC_DELETE: (id: number) => `/epic/${id}`,
  EPIC_DOWNLOAD: (managerId: number) => `/epic/manager/${managerId}/download`,
  
  // Stories
  STORIES: '/story/get',
  STORY_BY_ID: (id: number) => `/story/${id}`,
  STORY_CREATE: '/story/create',
  STORY_UPDATE: (id: number) => `/story/${id}`,
  STORY_DELETE: (id: number) => `/story/${id}`,
  STORY_COMPLETE: (id: number) => `/story/${id}/complete`,
  STORY_DOWNLOAD: (managerId: number) => `/story/manager/${managerId}/download`,
  
  // Clients
  CLIENTS: '/client/get',
  CLIENT_BY_ID: (id: number) => `/client/${id}`,
  CLIENT_CREATE: '/client/create',
  CLIENT_UPDATE: (id: number) => `/client/${id}`,
  CLIENT_DELETE: (id: number) => `/client/${id}`,
  CLIENT_DOWNLOAD: '/client/download',
  
  // SLA Rules
  SLA_RULES: '/sla/get',
  SLA_RULE_BY_ID: (id: number) => `/sla/${id}`,
  SLA_RULE_CREATE: '/sla/create',
  SLA_RULE_UPDATE: (id: number) => `/sla/${id}`,
  SLA_RULE_DELETE: (id: number) => `/sla/${id}`,
  SLA_DOWNLOAD: '/sla/download',
  
  // Reports
  REPORT_PROJECTS: (status: string, type: string) => `/report/projects/${status}?type=${type}`,
  REPORT_EPICS: (status: string, type: string) => `/report/epics/${status}?type=${type}`,
  REPORT_STORIES: (status: string, type: string) => `/report/stories/${status}?type=${type}`,

  // Notifications
  NOTIFICATIONS_RECENT: '/notifications/recent',
  NOTIFICATIONS_UNREAD: '/notifications/unread',
  NOTIFICATIONS_UNREAD_COUNT: '/notifications/unread-count',
  NOTIFICATIONS_PAGINATED: '/notifications/paginated',
  NOTIFICATIONS_MARK_ALL_READ: '/notifications/mark-all-read',
  NOTIFICATIONS_MARK_READ: (id: number) => `/notifications/${id}/read`,
  NOTIFICATIONS_DELETE: (id: number) => `/notifications/${id}`,
  NOTIFICATIONS_CLEANUP: '/notifications/cleanup',
  NOTIFICATIONS_CREATE: '/notifications/create',
  NOTIFICATIONS_CREATE_BATCH: '/notifications/create-batch',

  // Email
  EMAIL_SEND_SIMPLE: '/email/send/simple',
  EMAIL_SEND_ADVANCED: '/email/send/advanced',
  EMAIL_SEND_HTML: '/email/send/html',
  EMAIL_TEST: '/email/test',
  EMAIL_HEALTH: '/email/health',

  // Files
  FILE_UPLOAD: '/files/upload',
  FILE_UPLOAD_MULTIPLE: '/files/upload-multiple',
  FILE_DOWNLOAD: (fileName: string) => `/files/download/${fileName}`,
  FILE_DELETE: (fileName: string) => `/files/delete/${fileName}`,

  // Time Logs (outside /api/v1)
  TIME_LOGS: '/api/timelogs',
  TIME_LOGS_BY_STORY: (storyId: number) => `/api/timelogs/story/${storyId}`,
  TIME_LOGS_BY_USER: (userId: number) => `/api/timelogs/user/${userId}`,
  TIME_LOGS_TOTAL_BY_STORY: (storyId: number) => `/api/timelogs/story/${storyId}/total`,
  TIME_LOGS_TOTAL_BY_USER: (userId: number) => `/api/timelogs/user/${userId}/total`,
  TIME_LOGS_UPDATE: (id: number) => `/api/timelogs/${id}`,
  TIME_LOGS_DELETE: (id: number) => `/api/timelogs/${id}`,
};

// Storage keys
export const STORAGE_KEYS = {
  TOKEN: 'elara_token',
  USER: 'elara_user',
};

// Access levels
export const ACCESS_LEVELS = {
  USER: 1,
  EMPLOYEE: 2,
  MANAGER: 3,
  SENIOR_MANAGER: 4,
  ADMIN: 5,
};

// Roles
export const ROLES = {
  USER: 'USER',
  EMPLOYEE: 'EMPLOYEE',
  MANAGER: 'MANAGER',
  ADMIN: 'ADMIN',
};
