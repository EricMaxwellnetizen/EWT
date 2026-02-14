import React, { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { UploadCloud, Download, Trash2, FileText } from 'lucide-react';
import { fileService, type UploadedFileInfo } from '../../services/fileService';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { useAuthStore } from '../../store/authStore';
import toast from 'react-hot-toast';

export const FilesPage: React.FC = () => {
  const { hasAccessLevel } = useAuthStore();
  const [uploadedFiles, setUploadedFiles] = useState<UploadedFileInfo[]>([]);
  const [downloadName, setDownloadName] = useState('');
  const [deleteName, setDeleteName] = useState('');

  const uploadSingleMutation = useMutation({
    mutationFn: fileService.uploadSingle,
    onSuccess: (data) => {
      setUploadedFiles((prev) => [data, ...prev]);
      toast.success('File uploaded successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.error || 'Failed to upload file');
    },
  });

  const uploadMultipleMutation = useMutation({
    mutationFn: fileService.uploadMultiple,
    onSuccess: (data) => {
      setUploadedFiles((prev) => [...data.files, ...prev]);
      toast.success('Files uploaded successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.error || 'Failed to upload files');
    },
  });

  const downloadMutation = useMutation({
    mutationFn: fileService.downloadByName,
    onSuccess: () => {
      toast.success('Download started');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.error || 'Failed to download file');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: fileService.deleteByName,
    onSuccess: () => {
      toast.success('File deleted');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.error || 'Failed to delete file');
    },
  });

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">File Management</h1>
        <p className="text-gray-600 mt-1">Upload, download, and manage files</p>
      </div>

      <Card className="p-6 space-y-4">
        <div className="flex items-center gap-3">
          <UploadCloud className="w-6 h-6 text-primary-600" />
          <h2 className="text-xl font-semibold text-gray-900">Upload Files</h2>
        </div>

        <div className="flex flex-col gap-3">
          <label className="text-sm font-medium text-gray-700">Single File</label>
          <input
            type="file"
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) uploadSingleMutation.mutate(file);
            }}
          />
        </div>

        <div className="flex flex-col gap-3">
          <label className="text-sm font-medium text-gray-700">Multiple Files</label>
          <input
            type="file"
            multiple
            onChange={(e) => {
              const files = Array.from(e.target.files || []);
              if (files.length > 0) uploadMultipleMutation.mutate(files);
            }}
          />
        </div>
      </Card>

      <Card className="p-6 space-y-4">
        <div className="flex items-center gap-3">
          <Download className="w-6 h-6 text-emerald-600" />
          <h2 className="text-xl font-semibold text-gray-900">Download File</h2>
        </div>
        <div className="flex items-center gap-3">
          <Input
            label="File Name"
            value={downloadName}
            onChange={(e) => setDownloadName(e.target.value)}
            placeholder="Enter file name to download"
          />
          <Button
            variant="secondary"
            onClick={() => downloadMutation.mutate(downloadName)}
            disabled={!downloadName}
            isLoading={downloadMutation.isPending}
          >
            Download
          </Button>
        </div>
      </Card>

      <Card className="p-6 space-y-4">
        <div className="flex items-center gap-3">
          <Trash2 className="w-6 h-6 text-red-600" />
          <h2 className="text-xl font-semibold text-gray-900">Delete File</h2>
        </div>
        <div className="flex items-center gap-3">
          <Input
            label="File Name"
            value={deleteName}
            onChange={(e) => setDeleteName(e.target.value)}
            placeholder="Enter file name to delete"
          />
          <Button
            variant="danger"
            onClick={() => deleteMutation.mutate(deleteName)}
            disabled={!deleteName || !hasAccessLevel(3)}
            isLoading={deleteMutation.isPending}
          >
            Delete
          </Button>
        </div>
        {!hasAccessLevel(3) && (
          <p className="text-sm text-gray-500">You need manager access to delete files.</p>
        )}
      </Card>

      {uploadedFiles.length > 0 && (
        <Card className="p-6">
          <div className="flex items-center gap-3 mb-4">
            <FileText className="w-6 h-6 text-gray-500" />
            <h2 className="text-xl font-semibold text-gray-900">Recently Uploaded</h2>
          </div>
          <div className="divide-y divide-gray-100">
            {uploadedFiles.map((file) => (
              <div key={file.fileName} className="py-3 flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-900">{file.fileName}</p>
                  <p className="text-xs text-gray-500">{file.fileType || 'unknown'} • {file.size || 'n/a'}</p>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => downloadMutation.mutate(file.fileName)}
                >
                  Download
                </Button>
              </div>
            ))}
          </div>
        </Card>
      )}
    </div>
  );
};
