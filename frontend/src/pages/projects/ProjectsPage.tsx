import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Search, Trash2, Pencil, Download, CheckCircle2 } from 'lucide-react';
import { Card, CardBody, CardHeader } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { Loader } from '../../components/ui/Loader';
import { Modal, ModalFooter } from '../../components/ui/Modal';
import { Select } from '../../components/ui/Select';
import { projectService } from '../../services/projectService';
import { clientService } from '../../services/clientService';
import { userService } from '../../services/userService';
import { useAuthStore } from '../../store/authStore';
import { ActionGuard } from '../../components/auth/RoleGuard';
import { useUndoRedo } from '../../hooks/useUndoRedo';
import { formatDate } from '../../utils/helpers';
import toast from 'react-hot-toast';
import type { ProjectFormData } from '../../types';

export const ProjectsPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user, hasAccessLevel, hasHydrated } = useAuthStore();
  const { trackMutation } = useUndoRedo();
  
  const [searchTerm, setSearchTerm] = useState('');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [formData, setFormData] = useState<ProjectFormData>({
    name: '',
    clientId: 0,
    description: '',
    endDate: '',
  });
  const [editingProject, setEditingProject] = useState<any | null>(null);

  // Fetch data
  const { data: projects, isLoading } = useQuery({
    queryKey: ['projects'],
    queryFn: projectService.getAll,
    enabled: hasHydrated,
  });

  const { data: clients } = useQuery({
    queryKey: ['clients'],
    queryFn: clientService.getAll,
    enabled: hasHydrated,
  });

  const { data: users } = useQuery({
    queryKey: ['users'],
    queryFn: userService.getAll,
    enabled: hasHydrated,
  });

  // Create mutation
  const createMutation = useMutation({
    mutationFn: projectService.create,
    onSuccess: (newProject) => {
      trackMutation({
        description: `Created project "${newProject.name}"`,
        entityType: 'project',
        entityId: newProject.id!,
        newState: newProject,
        onUndo: async () => {
          await projectService.delete(newProject.id!);
        },
        onRedo: async () => {
          await projectService.create(newProject);
        },
        queryKeyToInvalidate: ['projects'],
      }).catch(console.error);

      queryClient.invalidateQueries({ queryKey: ['projects'] });
      toast.success('Project created successfully!');
      setIsCreateModalOpen(false);
      resetForm();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to create project');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<ProjectFormData> }) =>
      projectService.update(id, data),
    onSuccess: (_updated, variables) => {
      const previous = editingProject;
      if (previous) {
        trackMutation({
          description: `Updated project "${previous?.name}"`,
          entityType: 'project',
          entityId: variables.id,
          previousState: previous,
          newState: { ...previous, ...variables.data },
          onUndo: async () => {
            const payload: Partial<ProjectFormData> = {
              name: previous?.name,
              clientId: previous?.clientId ?? previous?.client_id?.id,
              managerId: previous?.managerId ?? previous?.manager_id?.id ?? previous?.manager_id,
              deliverables: previous?.deliverables,
              deadline: previous?.deadline,
            };
            await projectService.update(variables.id, payload);
          },
          onRedo: async () => {
            await projectService.update(variables.id, variables.data);
          },
          queryKeyToInvalidate: ['projects'],
        }).catch(console.error);
      }

      queryClient.invalidateQueries({ queryKey: ['projects'] });
      toast.success('Project updated successfully!');
      setIsCreateModalOpen(false);
      setEditingProject(null);
      resetForm();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to update project');
    },
  });

  const downloadMutation = useMutation({
    mutationFn: (managerId: number) => projectService.downloadManagerProjects(managerId),
    onSuccess: () => {
      toast.success('Projects document downloaded successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to download projects document');
    },
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: ({ id }: { id: number; project: any }) => projectService.delete(id),
    onSuccess: async (_data, variables) => {
      const project = variables.project;
      let recreatedProjectId: number | null = null;
      
      const projectPayload: any = {
        name: project?.name,
        clientId: project?.clientId ?? project?.client_id?.client_id ?? project?.client_id,
        managerId: project?.managerId ?? project?.manager_id?.id ?? project?.manager_id,
        deliverables: project?.deliverables,
        deadline: project?.deadline,
      };

      trackMutation({
        description: `Deleted project "${project?.name}"`,
        entityType: 'project',
        entityId: variables.id,
        previousState: project,
        onUndo: async () => {
          const newProject = await projectService.create(projectPayload);
          recreatedProjectId = newProject.projectId || newProject.id;
        },
        onRedo: async () => {
          if (recreatedProjectId) {
            await projectService.delete(recreatedProjectId);
          } else {
            await projectService.delete(variables.id);
          }
        },
        queryKeyToInvalidate: ['projects'],
      }).catch(console.error);

      queryClient.invalidateQueries({ queryKey: ['projects'] });
      toast.success('Project deleted successfully!');
    },
  });

  const resetForm = () => {
    setFormData({
      name: '',
      clientId: 0,
      description: '',
      endDate: '',
    });
    setEditingProject(null);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingProject?.id) {
      updateMutation.mutate({ id: editingProject.id, data: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (id: number) => {
    const project = projects?.find((p) => p.id === id);
    if (window.confirm('Are you sure you want to delete this project?')) {
      deleteMutation.mutate({ id, project });
    }
  };

  const handleEdit = (project: any) => {
    setEditingProject(project);
    setFormData({
      name: project?.name || '',
      clientId: project?.clientId ?? project?.client_id?.id ?? 0,
      description: project?.description || '',
      endDate: project?.endDate || '',
    });
    setIsCreateModalOpen(true);
  };

  // Filter projects
  const filteredProjects = projects?.filter(project =>
    project.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const canCreate = hasAccessLevel(3);
  const canDelete = hasAccessLevel(4);
  const canDownload = hasAccessLevel(3) && Boolean(user?.id);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-96">
        <Loader size="lg" text="Loading projects..." />
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gradient">Projects</h1>
          <p className="text-slate-600 dark:text-gray-400 mt-1">Manage your projects and workflows</p>
        </div>
        <div className="flex gap-3">
          {canDownload && (
            <Button
              variant="secondary"
              icon={<Download className="w-5 h-5" />}
              onClick={() => downloadMutation.mutate(user?.id!)}
              isLoading={downloadMutation.isPending}
            >
              Download My Projects
            </Button>
          )}
          {canCreate && (
            <Button
              variant="primary"
              icon={<Plus className="w-5 h-5" />}
              onClick={() => setIsCreateModalOpen(true)}
            >
              New Project
            </Button>
          )}
        </div>
      </div>

      {/* Filters */}
      <Card glass>
        <CardBody>
          <div className="flex gap-4">
            <div className="flex-1">
              <Input
                placeholder="Search projects..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                icon={<Search className="w-5 h-5" />}
              />
            </div>
          </div>
        </CardBody>
      </Card>

      {/* Projects Grid */}
      {filteredProjects && filteredProjects.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredProjects.map((project) => (
            <Card
              key={project.id}
              hover
              glass
              onClick={() => navigate(`/projects/${project.id}`)}
            >
              <CardHeader>
                <div className="flex items-start justify-between">
                  <h3 className="text-lg font-semibold text-slate-900 dark:text-gray-100 flex-1 pr-2">
                    {project.name}
                  </h3>
                  {(project as any).isEnd ? (
                    <Badge variant="success">
                      <CheckCircle2 className="w-3 h-3 mr-1 inline" />
                      Completed
                    </Badge>
                  ) : (
                    <Badge variant="info">
                      Active
                    </Badge>
                  )}
                </div>
              </CardHeader>
              <CardBody>
                <div className="space-y-3">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-slate-600 dark:text-gray-400">End Date:</span>
                    <span className="font-medium text-slate-900 dark:text-gray-100">
                      {formatDate(project.endDate)}
                    </span>
                  </div>
                  
                  {project.description && (
                    <p className="text-sm text-slate-600 dark:text-gray-400 line-clamp-2">
                      {project.description}
                    </p>
                  )}

                  {canDelete && (
                    <div className="flex gap-2 pt-3 border-t border-slate-200">
                      <Button
                        size="sm"
                        variant="secondary"
                        icon={<Pencil className="w-4 h-4" />}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleEdit(project);
                        }}
                      >
                        Edit
                      </Button>
                      <Button
                        size="sm"
                        variant="danger"
                        icon={<Trash2 className="w-4 h-4" />}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleDelete(project.id);
                        }}
                        isLoading={deleteMutation.isPending}
                      >
                        Delete
                      </Button>
                    </div>
                  )}
                </div>
              </CardBody>
            </Card>
          ))}
        </div>
      ) : (
        <Card glass>
          <CardBody>
            <div className="text-center py-12">
              <p className="text-slate-600 dark:text-gray-400 mb-4">
                {searchTerm
                  ? `No projects match "${searchTerm}". Try a different search term.`
                  : 'No projects found. Create your first project to get started!'}
              </p>
              {canCreate && !searchTerm && (
                <Button
                  variant="primary"
                  icon={<Plus className="w-5 h-5" />}
                  onClick={() => setIsCreateModalOpen(true)}
                >
                  Create First Project
                </Button>
              )}
            </div>
          </CardBody>
        </Card>
      )}

      {/* Create Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={() => {
          setIsCreateModalOpen(false);
          resetForm();
        }}
        title={editingProject ? 'Edit Project' : 'Create New Project'}
        size="lg"
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Project Name"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            required
          />

          <Select
            label="Client"
            value={formData.clientId}
            onChange={(e) => setFormData({ ...formData, clientId: Number(e.target.value) })}
            options={clients?.map(c => ({ value: (c.clientId ?? c.id)!, label: c.name })) || []}
            required
          />

          <Select
            label="Manager"
            value={formData.managerId}
            onChange={(e) => setFormData({ ...formData, managerId: Number(e.target.value) })}
            options={users?.map(u => ({ 
              value: u.id!, 
              label: `${u.firstName || u.username || ''} ${u.lastName || ''} (ID: ${u.id}) - ${u.role}` 
            })) || []}
            required
          />

          <Input
            label="End Date"
            type="date"
            value={formData.endDate}
            onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
          />

          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-gray-300 mb-2">
              Deliverables
            </label>
            <textarea
              className="input min-h-[100px]"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Describe the project deliverables..."
            />
          </div>

          <ModalFooter>
            <Button
              type="button"
              variant="secondary"
              onClick={() => setIsCreateModalOpen(false)}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              isLoading={createMutation.isPending || updateMutation.isPending}
            >
              {editingProject ? 'Save Changes' : 'Create Project'}
            </Button>
          </ModalFooter>
        </form>
      </Modal>
    </div>
  );
};
