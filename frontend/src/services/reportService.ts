import { download } from '../lib/api';
import { ENDPOINTS } from '../config/constants';

export const reportService = {
  // Projects Report
  exportProjectsExcel: (): Promise<void> => {
    return download(ENDPOINTS.REPORT_PROJECTS('unfinished', 'excel'), 'projects_report.xlsx');
  },
  exportProjectsWord: (): Promise<void> => {
    return download(ENDPOINTS.REPORT_PROJECTS('unfinished', 'word'), 'projects_report.docx');
  },
  exportProjectsPdf: (): Promise<void> => {
    return download(ENDPOINTS.REPORT_PROJECTS('unfinished', 'pdf'), 'projects_report.pdf');
  },

  // Epics Report
  exportEpicsExcel: (): Promise<void> => {
    return download(ENDPOINTS.REPORT_EPICS('unfinished', 'excel'), 'epics_report.xlsx');
  },
  exportEpicsWord: (): Promise<void> => {
    return download(ENDPOINTS.REPORT_EPICS('unfinished', 'word'), 'epics_report.docx');
  },
  exportEpicsPdf: (): Promise<void> => {
    return download(ENDPOINTS.REPORT_EPICS('unfinished', 'pdf'), 'epics_report.pdf');
  },

  // Stories Report
  exportStoriesExcel: (): Promise<void> => {
    return download(ENDPOINTS.REPORT_STORIES('unfinished', 'excel'), 'stories_report.xlsx');
  },
  exportStoriesWord: (): Promise<void> => {
    return download(ENDPOINTS.REPORT_STORIES('unfinished', 'word'), 'stories_report.docx');
  },
  exportStoriesPdf: (): Promise<void> => {
    return download(ENDPOINTS.REPORT_STORIES('unfinished', 'pdf'), 'stories_report.pdf');
  },

  // Legacy methods
  downloadProjectReport: (status: 'finished' | 'unfinished', type: 'excel' | 'word' | 'pdf'): Promise<void> => {
    const filename = `projects_${status}.${type === 'excel' ? 'xlsx' : type === 'word' ? 'docx' : 'pdf'}`;
    return download(ENDPOINTS.REPORT_PROJECTS(status, type), filename);
  },

  downloadEpicReport: (status: 'finished' | 'unfinished', type: 'excel' | 'word' | 'pdf'): Promise<void> => {
    const filename = `epics_${status}.${type === 'excel' ? 'xlsx' : type === 'word' ? 'docx' : 'pdf'}`;
    return download(ENDPOINTS.REPORT_EPICS(status, type), filename);
  },

  downloadStoryReport: (status: 'finished' | 'unfinished', type: 'excel' | 'word' | 'pdf'): Promise<void> => {
    const filename = `stories_${status}.${type === 'excel' ? 'xlsx' : type === 'word' ? 'docx' : 'pdf'}`;
    return download(ENDPOINTS.REPORT_STORIES(status, type), filename);
  },
};
