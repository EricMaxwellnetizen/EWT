import React, { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { FileText, FileSpreadsheet, Download, CheckCircle } from 'lucide-react';
import { reportService } from '../../services/reportService';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { useAuthStore } from '../../store/authStore';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';

type ReportType = 'projects' | 'epics' | 'stories';
type ExportFormat = 'excel' | 'word' | 'pdf';
type ReportStatus = 'finished' | 'unfinished';

interface ReportOption {
  type: ReportType;
  title: string;
  description: string;
  icon: React.ReactNode;
  color: string;
}

export const ReportsPage: React.FC = () => {
  const { hasAccessLevel } = useAuthStore();
  const [activeReport, setActiveReport] = useState<ReportType | null>(null);
  const [status, setStatus] = useState<ReportStatus>('unfinished');

  const reportOptions: ReportOption[] = [
    {
      type: 'projects',
      title: 'Projects Report',
      description: 'Export all projects with their details, status, and timelines',
      icon: <FileText className="w-8 h-8" />,
      color: 'from-blue-500 to-blue-600',
    },
    {
      type: 'epics',
      title: 'Epics Report',
      description: 'Export all epics with project associations and progress tracking',
      icon: <FileSpreadsheet className="w-8 h-8" />,
      color: 'from-purple-500 to-purple-600',
    },
    {
      type: 'stories',
      title: 'Stories Report',
      description: 'Export all stories with detailed assignments and status information',
      icon: <FileText className="w-8 h-8" />,
      color: 'from-green-500 to-green-600',
    },
  ];

  // Mutations for different export formats
  const excelMutation = useMutation({
    mutationFn: ({ type, status }: { type: ReportType; status: ReportStatus }) => {
      switch (type) {
        case 'projects':
          return reportService.downloadProjectReport(status, 'excel');
        case 'epics':
          return reportService.downloadEpicReport(status, 'excel');
        case 'stories':
          return reportService.downloadStoryReport(status, 'excel');
      }
    },
    onSuccess: () => {
      toast.success('Excel report downloaded successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to generate Excel report');
    },
  });

  const wordMutation = useMutation({
    mutationFn: ({ type, status }: { type: ReportType; status: ReportStatus }) => {
      switch (type) {
        case 'projects':
          return reportService.downloadProjectReport(status, 'word');
        case 'epics':
          return reportService.downloadEpicReport(status, 'word');
        case 'stories':
          return reportService.downloadStoryReport(status, 'word');
      }
    },
    onSuccess: () => {
      toast.success('Word report downloaded successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to generate Word report');
    },
  });

  const pdfMutation = useMutation({
    mutationFn: ({ type, status }: { type: ReportType; status: ReportStatus }) => {
      switch (type) {
        case 'projects':
          return reportService.downloadProjectReport(status, 'pdf');
        case 'epics':
          return reportService.downloadEpicReport(status, 'pdf');
        case 'stories':
          return reportService.downloadStoryReport(status, 'pdf');
      }
    },
    onSuccess: () => {
      toast.success('PDF report downloaded successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to generate PDF report');
    },
  });

  const handleExport = (type: ReportType, format: ExportFormat) => {
    setActiveReport(type);
    switch (format) {
      case 'excel':
        excelMutation.mutate({ type, status });
        break;
      case 'word':
        wordMutation.mutate({ type, status });
        break;
      case 'pdf':
        pdfMutation.mutate({ type, status });
        break;
    }
  };

  const isLoading = excelMutation.isPending || wordMutation.isPending || pdfMutation.isPending;

  // Only users with access level 2+ can generate reports
  const canGenerateReports = hasAccessLevel(2);

  if (!canGenerateReports) {
    return (
      <div className="p-6">
        <Card className="p-12 text-center">
          <FileText className="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-2xl font-semibold text-gray-900 mb-2">Access Denied</h3>
          <p className="text-gray-600">
            You need Employee level access or higher to generate reports.
          </p>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Reports</h1>
        <p className="text-gray-600 mt-1">Export data in multiple formats for analysis and documentation</p>
      </div>

      <div className="flex items-center gap-4">
        <span className="text-sm text-gray-700">Status:</span>
        <select
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm"
          value={status}
          onChange={(e) => setStatus(e.target.value as ReportStatus)}
        >
          <option value="unfinished">Unfinished</option>
          <option value="finished">Finished</option>
        </select>
      </div>

      {/* Info Card */}
      <Card className="p-4 bg-blue-50 border-blue-200">
        <div className="flex items-start gap-3">
          <CheckCircle className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" />
          <div className="flex-1">
            <h4 className="font-semibold text-blue-900 mb-1">Available Export Formats</h4>
            <p className="text-sm text-blue-800">
              Choose from Excel (.xlsx), Word (.docx), or PDF formats for your reports.
              All reports include comprehensive data with timestamps and user information.
            </p>
          </div>
        </div>
      </Card>

      {/* Report Cards */}
      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {reportOptions.map((report, index) => (
          <motion.div
            key={report.type}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.1 }}
          >
            <Card className="p-6 hover:shadow-xl transition-all duration-300">
              <div
                className={`w-16 h-16 rounded-2xl bg-gradient-to-br ${report.color} flex items-center justify-center text-white mb-4`}
              >
                {report.icon}
              </div>

              <h3 className="text-xl font-semibold text-gray-900 mb-2">{report.title}</h3>
              <p className="text-gray-600 mb-6">{report.description}</p>

              <div className="space-y-2">
                <p className="text-sm font-medium text-gray-700 mb-3">Export as:</p>

                {/* Excel Export */}
                <Button
                  variant="secondary"
                  className="w-full justify-between"
                  onClick={() => handleExport(report.type, 'excel')}
                  isLoading={isLoading && activeReport === report.type && excelMutation.isPending}
                  disabled={isLoading && activeReport !== report.type}
                  icon={<FileSpreadsheet className="w-4 h-4" />}
                >
                  <span>Excel</span>
                  <Download className="w-4 h-4" />
                </Button>

                {/* Word Export */}
                <Button
                  variant="secondary"
                  className="w-full justify-between"
                  onClick={() => handleExport(report.type, 'word')}
                  isLoading={isLoading && activeReport === report.type && wordMutation.isPending}
                  disabled={isLoading && activeReport !== report.type}
                  icon={<FileText className="w-4 h-4" />}
                >
                  <span>Word</span>
                  <Download className="w-4 h-4" />
                </Button>

                {/* PDF Export */}
                <Button
                  variant="secondary"
                  className="w-full justify-between"
                  onClick={() => handleExport(report.type, 'pdf')}
                  isLoading={isLoading && activeReport === report.type && pdfMutation.isPending}
                  disabled={isLoading && activeReport !== report.type}
                  icon={<FileText className="w-4 h-4" />}
                >
                  <span>PDF</span>
                  <Download className="w-4 h-4" />
                </Button>
              </div>
            </Card>
          </motion.div>
        ))}
      </div>

      {/* Additional Info */}
      <Card className="p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-3">Report Details</h3>
        <div className="space-y-2 text-sm text-gray-600">
          <div className="flex items-start gap-2">
            <CheckCircle className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
            <p>
              <strong>Excel Reports:</strong> Perfect for data analysis, includes all fields in a
              structured table format with filtering capabilities.
            </p>
          </div>
          <div className="flex items-start gap-2">
            <CheckCircle className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
            <p>
              <strong>Word Reports:</strong> Ideal for documentation and presentations, formatted
              with headers and organized sections.
            </p>
          </div>
          <div className="flex items-start gap-2">
            <CheckCircle className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
            <p>
              <strong>PDF Reports:</strong> Best for sharing and printing, provides a professional
              layout with consistent formatting.
            </p>
          </div>
        </div>
      </Card>
    </div>
  );
};
