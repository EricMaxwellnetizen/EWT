import { format, formatDistance, parseISO } from 'date-fns';

export const formatDate = (date: string | Date | undefined): string => {
  if (!date) return 'N/A';
  const parsedDate = typeof date === 'string' ? parseISO(date) : date;
  return format(parsedDate, 'MMM dd, yyyy');
};

export const formatDateTime = (date: string | Date | undefined): string => {
  if (!date) return 'N/A';
  const parsedDate = typeof date === 'string' ? parseISO(date) : date;
  return format(parsedDate, 'MMM dd, yyyy HH:mm');
};

export const formatRelativeTime = (date: string | Date | undefined): string => {
  if (!date) return 'N/A';
  const parsedDate = typeof date === 'string' ? parseISO(date) : date;
  return formatDistance(parsedDate, new Date(), { addSuffix: true });
};

export const getInitials = (name: string): string => {
  return name
    .split(' ')
    .map(word => word[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
};

export const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
};

export const truncate = (str: string, length: number): string => {
  if (!str) return '';
  return str.length > length ? str.substring(0, length) + '...' : str;
};

export const capitalize = (str: string): string => {
  if (!str) return '';
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
};

export const getStatusColor = (status: string): string => {
  const statusMap: Record<string, string> = {
    approved: 'success',
    completed: 'success',
    pending: 'warning',
    'in-progress': 'info',
    rejected: 'danger',
    failed: 'danger',
  };
  return statusMap[status.toLowerCase()] || 'gray';
};

export const getPriorityColor = (priority: string): string => {
  const priorityMap: Record<string, string> = {
    high: 'danger',
    medium: 'warning',
    low: 'success',
  };
  return priorityMap[priority.toLowerCase()] || 'gray';
};

export const downloadBlob = (blob: Blob, filename: string) => {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
};

export const downloadFile = (blob: Blob, filename: string) => {
  downloadBlob(blob, filename);
};
