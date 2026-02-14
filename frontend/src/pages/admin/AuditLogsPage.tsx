import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Activity, Filter, RefreshCw, Download, Clock, User, Database } from 'lucide-react';
import { auditLogService } from '../../services/auditLogService';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Badge } from '../../components/ui/Badge';
import { formatDate } from '../../utils/helpers';
import toast from 'react-hot-toast';

export const AuditLogsPage: React.FC = () => {
  const [page, setPage] = useState(0);
  const [filters, setFilters] = useState({
    entityType: '',
    operation: '',
    username: '',
    startDate: '',
    endDate: '',
  });
  const [showFilters, setShowFilters] = useState(false);
  const [autoRefresh, setAutoRefresh] = useState(true);

  // Fetch audit logs
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['auditLogs', page, filters],
    queryFn: async () => {
      if (filters.entityType || filters.operation || filters.username || filters.startDate || filters.endDate) {
        return await auditLogService.searchLogs(filters, page, 50);
      }
      return await auditLogService.getAllLogs(page, 50);
    },
    refetchInterval: autoRefresh ? 5000 : false, // Auto-refresh every 5 seconds
  });

  // Fetch statistics
  const { data: stats } = useQuery({
    queryKey: ['auditLogStats'],
    queryFn: () => auditLogService.getStatistics(),
    refetchInterval: autoRefresh ? 10000 : false,
  });

  const logs = data?.content || [];
  const totalPages = data?.totalPages || 1;

  const getOperationColor = (operation: string): 'success' | 'warning' | 'danger' | 'info' | 'gray' => {
    switch (operation) {
      case 'CREATE': return 'success';
      case 'UPDATE': return 'warning';
      case 'DELETE': return 'danger';
      case 'READ': return 'info';
      default: return 'gray';
    }
  };

  const handleExport = () => {
    toast.success('Export feature will be available soon');
  };

  const handleClearFilters = () => {
    setFilters({
      entityType: '',
      operation: '',
      username: '',
      startDate: '',
      endDate: '',
    });
    setPage(0);
  };

  const parseChanges = (changes: string | null) => {
    if (!changes) return null;
    try {
      return JSON.parse(changes);
    } catch {
      return null;
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-900">System Activity Logs</h1>
          <p className="text-slate-600 mt-1">Track every change made to your workspace</p>
        </div>
        <div className="flex items-center gap-3">
          <Button
            onClick={() => setShowFilters(!showFilters)}
            variant="secondary"
            icon={<Filter className="w-4 h-4" />}
          >
            {showFilters ? 'Hide Filters' : 'Show Filters'}
          </Button>
          <Button
            onClick={() => refetch()}
            variant="secondary"
            icon={<RefreshCw className="w-4 h-4" />}
          >
            Refresh
          </Button>
          <Button
            onClick={handleExport}
            variant="secondary"
            icon={<Download className="w-4 h-4" />}
          >
            Export
          </Button>
        </div>
      </div>

      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <Card className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-slate-600">Total Records</p>
                <p className="text-2xl font-bold text-slate-900">{stats.totalLogs.toLocaleString()}</p>
              </div>
              <Database className="w-10 h-10 text-primary-600" />
            </div>
          </Card>
          <Card className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-slate-600">New Items</p>
                <p className="text-2xl font-bold text-green-600">{stats.byOperation.CREATE || 0}</p>
              </div>
              <Activity className="w-10 h-10 text-green-600" />
            </div>
          </Card>
          <Card className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-slate-600">Modifications</p>
                <p className="text-2xl font-bold text-yellow-600">{stats.byOperation.UPDATE || 0}</p>
              </div>
              <Activity className="w-10 h-10 text-yellow-600" />
            </div>
          </Card>
          <Card className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-slate-600">Removals</p>
                <p className="text-2xl font-bold text-red-600">{stats.byOperation.DELETE || 0}</p>
              </div>
              <Activity className="w-10 h-10 text-red-600" />
            </div>
          </Card>
        </div>
      )}

      <div className="flex items-center justify-between bg-slate-50 p-4 rounded-lg">
        <div className="flex items-center gap-2">
          <Clock className="w-5 h-5 text-slate-600" />
          <span className="text-sm font-medium text-slate-700">
            Refresh automatically every 5 seconds
          </span>
        </div>
        <label className="relative inline-flex items-center cursor-pointer">
          <input
            type="checkbox"
            className="sr-only peer"
            checked={autoRefresh}
            onChange={(e) => setAutoRefresh(e.target.checked)}
          />
          <div className="w-11 h-6 bg-slate-300 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-primary-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary-600"></div>
        </label>
      </div>

      {showFilters && (
        <Card className="p-6">
          <h3 className="text-lg font-semibold mb-4">Filter Options</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Resource Type</label>
              <input
                type="text"
                value={filters.entityType}
                onChange={(e) => setFilters({ ...filters, entityType: e.target.value })}
                placeholder="Project, User, Client..."
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Action Type</label>
              <select
                value={filters.operation}
                onChange={(e) => setFilters({ ...filters, operation: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              >
                <option value="">All Actions</option>
                <option value="CREATE">Created</option>
                <option value="UPDATE">Modified</option>
                <option value="DELETE">Deleted</option>
                <option value="READ">Viewed</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Team Member</label>
              <input
                type="text"
                value={filters.username}
                onChange={(e) => setFilters({ ...filters, username: e.target.value })}
                placeholder="Search by name"
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">From</label>
              <input
                type="datetime-local"
                value={filters.startDate}
                onChange={(e) => setFilters({ ...filters, startDate: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">To</label>
              <input
                type="datetime-local"
                value={filters.endDate}
                onChange={(e) => setFilters({ ...filters, endDate: e.target.value })}
                className="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
              />
            </div>
            <div className="flex items-end">
              <Button onClick={handleClearFilters} variant="secondary" className="w-full">
                Reset Filters
              </Button>
            </div>
          </div>
        </Card>
      )}

      <Card className="overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-slate-50 border-b border-slate-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                  When
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                  Action
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                  Resource
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                  Who
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                  Location
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                  Details
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                  What Changed
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-slate-200">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-slate-500">
                    <div className="flex items-center justify-center">
                      <RefreshCw className="w-6 h-6 animate-spin mr-2" />
                      Fetching activity logs...
                    </div>
                  </td>
                </tr>
              ) : logs.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-slate-500">
                    No activity found for these filters
                  </td>
                </tr>
              ) : (
                logs.map((log) => {
                  const changes = parseChanges(log.changes);
                  return (
                    <tr key={log.id} className="hover:bg-slate-50 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-900">
                        {formatDate(log.timestamp)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <Badge variant={getOperationColor(log.operation)}>
                          {log.operation}
                        </Badge>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm">
                        <div className="font-medium text-slate-900">{log.entityType}</div>
                        <div className="text-slate-500">ID: {log.entityId}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center text-sm text-slate-900">
                          <User className="w-4 h-4 mr-2 text-slate-400" />
                          {log.username || 'System'}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-500">
                        {log.ipAddress || '-'}
                      </td>
                      <td className="px-6 py-4 text-sm text-slate-500">
                        {log.description || '-'}
                      </td>
                      <td className="px-6 py-4 text-sm">
                        {changes ? (
                          <details className="cursor-pointer">
                            <summary className="text-primary-600 hover:text-primary-700 font-medium">
                              See details
                            </summary>
                            <div className="mt-2 space-y-1 text-xs bg-slate-50 p-2 rounded">
                              {Object.entries(changes).map(([key, value]) => (
                                <div key={key}>
                                  <span className="font-medium">{key}:</span> {value as string}
                                </div>
                              ))}
                            </div>
                          </details>
                        ) : (
                          <span className="text-slate-400">None</span>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="px-6 py-4 border-t border-slate-200 flex items-center justify-between">
            <div className="text-sm text-slate-700">
              Page {page + 1} of {totalPages}
            </div>
            <div className="flex gap-2">
              <Button
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                variant="secondary"
                size="sm"
              >
                Previous
              </Button>
              <Button
                onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                disabled={page >= totalPages - 1}
                variant="secondary"
                size="sm"
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </Card>
    </div>
  );
};
