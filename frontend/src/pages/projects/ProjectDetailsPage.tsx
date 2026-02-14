import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, AlertCircle } from 'lucide-react';
import { projectService } from '../../services/projectService';
import { useAuthStore } from '../../store/authStore';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { Loader } from '../../components/ui/Loader';
import { formatDate } from '../../utils/helpers';

export const ProjectDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { hasHydrated } = useAuthStore();
  const projectId = id ? parseInt(id) : 0;

  const { data: project, isLoading, error, isError } = useQuery({
    queryKey: ['project', projectId],
    queryFn: () => projectService.getById(projectId),
    enabled: !!projectId && hasHydrated,
    retry: 1,
  });

  if (isLoading) {
    return <Loader />;
  }

  if (isError) {
    return (
      <div className="p-6">
        <Button 
          variant="ghost" 
          onClick={() => navigate('/projects')}
          className="mb-6"
          icon={<ArrowLeft className="w-4 h-4" />}
        >
          Back to Projects
        </Button>
        <Card className="p-12 text-center">
          <AlertCircle className="w-12 h-12 text-red-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Project Not Found</h3>
          <p className="text-gray-600 mb-4">{(error as any)?.message || 'The requested project could not be found.'}</p>
          <Button onClick={() => navigate('/projects')}>Return to Projects</Button>
        </Card>
      </div>
    );
  }

  if (!project) {
    return (
      <div className="p-6">
        <Button 
          variant="ghost" 
          onClick={() => navigate('/projects')}
          className="mb-6"
          icon={<ArrowLeft className="w-4 h-4" />}
        >
          Back to Projects
        </Button>
        <Card className="p-12 text-center">
          <AlertCircle className="w-12 h-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">Project Not Found</h3>
          <p className="text-gray-600">No project data available.</p>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <Button 
        variant="ghost" 
        onClick={() => navigate('/projects')}
        icon={<ArrowLeft className="w-4 h-4" />}
      >
        Back to Projects
      </Button>

      <div className="space-y-6">
        {/* Header */}
        <Card className="p-6">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">{project.name}</h1>
          <p className="text-gray-600">{project.description}</p>
        </Card>

        {/* Details Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <Card className="p-6">
            <h3 className="text-sm font-medium text-gray-500 mb-2">Start Date</h3>
            <p className="text-2xl font-bold text-gray-900">{formatDate(project.startDate)}</p>
          </Card>

          <Card className="p-6">
            <h3 className="text-sm font-medium text-gray-500 mb-2">End Date</h3>
            <p className="text-2xl font-bold text-gray-900">{formatDate(project.endDate)}</p>
          </Card>

          <Card className="p-6">
            <h3 className="text-sm font-medium text-gray-500 mb-2">Created</h3>
            <p className="text-sm text-gray-900">{formatDate(project.createdAt)}</p>
          </Card>
        </div>

        {/* Description Section */}
        {project.description && (
          <Card className="p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">Full Description</h3>
            <p className="text-gray-600 whitespace-pre-wrap">{project.description}</p>
          </Card>
        )}
      </div>
    </div>
  );
};
