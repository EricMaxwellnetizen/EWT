import api from '../lib/api';

export interface AuditLog {
  id: number;
  timestamp: string;
  entityType: string;
  entityId: number;
  operation: 'CREATE' | 'UPDATE' | 'DELETE' | 'READ';
  username: string | null;
  ipAddress: string | null;
  oldValue: string | null;
  newValue: string | null;
  changes: string | null;
  description: string | null;
}

export interface AuditLogPage {
  content: AuditLog[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export interface AuditLogStats {
  byOperation: Record<string, number>;
  byEntityType: Record<string, number>;
  totalLogs: number;
}

export const auditLogService = {
  // Get all logs with pagination
  async getAllLogs(page = 0, size = 50, sortBy = 'timestamp', sortDir = 'DESC'): Promise<AuditLogPage> {
    const response = await api.get('/admin/audit-logs', {
      params: { page, size, sortBy, sortDir }
    });
    return response.data;
  },

  // Search logs with filters
  async searchLogs(
    filters: {
      entityType?: string;
      operation?: string;
      username?: string;
      startDate?: string;
      endDate?: string;
    },
    page = 0,
    size = 50
  ): Promise<AuditLogPage> {
    const response = await api.get('/admin/audit-logs/search', {
      params: { ...filters, page, size }
    });
    return response.data;
  },

  // Get logs for a specific entity
  async getLogsForEntity(entityType: string, entityId: number, page = 0, size = 20): Promise<AuditLogPage> {
    const response = await api.get(`/admin/audit-logs/${entityType}/${entityId}`, {
      params: { page, size }
    });
    return response.data;
  },

  // Get recent logs
  async getRecentLogs(): Promise<AuditLog[]> {
    const response = await api.get('/admin/audit-logs/recent');
    return response.data;
  },

  // Get statistics
  async getStatistics(): Promise<AuditLogStats> {
    const response = await api.get('/admin/audit-logs/statistics');
    return response.data;
  },
};
