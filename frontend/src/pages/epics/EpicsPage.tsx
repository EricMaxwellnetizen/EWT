import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Search, Trash2, AlertCircle, Download, Pencil, CheckCircle2 } from 'lucide-react';
import { epicService } from '../../services/epicService';
import { projectService } from '../../services/projectService';
import { userService } from '../../services/userService';
import type { EpicDTO } from '../../types';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { Modal } from '../../components/ui/Modal';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Badge } from '../../components/ui/Badge';
import { Loader } from '../../components/ui/Loader';
import { useAuthStore } from '../../store/authStore';
import { useUndoRedo } from '../../hooks/useUndoRedo';
import toast from 'react-hot-toast';
import { motion } from 'framer-motion';
import { formatDate } from '../../utils/helpers';

export const EpicsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const { hasAccessLevel, user } = useAuthStore();
  const { trackMutation } = useUndoRedo();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterProject, setFilterProject] = useState<string>('');
  const [formData, setFormData] = useState<EpicDTO>({
    name: '',
    description: '',
    projectId: 0 as any,
    managerId: user?.id || 0,
    assignedToUserId: 0,
    estimatedStartDate: '',
    estimatedEndDate: '',
  });
  const [editingEpic, setEditingEpic] = useState<any | null>(null);

  // Queries
  const { hasHydrated } = useAuthStore();
  
  const { data: epicsData = [], isLoading } = useQuery({
    queryKey: ['epics'],
    queryFn: epicService.getAll,
    enabled: hasHydrated,
  });

  const { data: projectsData = [] } = useQuery({
    queryKey: ['projects'],
    queryFn: projectService.getAll,
    enabled: hasHydrated,
  });

  const { data: usersData = [] } = useQuery({
    queryKey: ['users'],
    queryFn: userService.getAll,
    enabled: hasHydrated,
  });

  const epics = Array.isArray(epicsData) ? epicsData : [];
  const projects = Array.isArray(projectsData) ? projectsData : [];
  const users = Array.isArray(usersData) ? usersData : [];

  // Mutations
  const createMutation = useMutation({
    mutationFn: epicService.create,
    onSuccess: async (newEpic) => {
      try {
        await trackMutation({
          description: `Created epic "${newEpic.name}"`,
          entityType: 'epic',
          entityId: newEpic.id!,
          newState: newEpic,
          onUndo: async () => {
            await epicService.delete(newEpic.id!);
          },
          onRedo: async () => {
            await epicService.create(newEpic as EpicDTO);
          },
          queryKeyToInvalidate: ['epics'],
        });
      } catch (error) {
        console.error(error);
      }

      queryClient.invalidateQueries({ queryKey: ['epics'] });
      toast.success('Epic created successfully');
      setIsModalOpen(false);
      resetForm();
    },
    onError: (error: any) => {
      // Global interceptor already shows toast for 400 errors
      console.error('Epic creation failed:', error.response?.data?.message || error.message);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: ({ id }: { id: number; epic: any }) => epicService.delete(id),
    onSuccess: async (_data, variables) => {
      const epic = variables.epic;
      let recreatedEpicId: number | null = null;
      
      const epicPayload: any = {
        name: epic?.name,
        description: epic?.description,
        projectId: epic?.projectId ?? epic?.project?.id,
        managerId: epic?.assignedToUserId ?? user?.id!,
        assignedToUserId: epic?.assignedToUserId ?? epic?.assignedToUser?.id,
        estimatedStartDate: epic?.estimatedStartDate,
        estimatedEndDate: epic?.estimatedEndDate,
      };

      try {
        await trackMutation({
          description: `Deleted epic "${epic?.name}"`,
          entityType: 'epic',
          entityId: variables.id,
          previousState: epic,
          onUndo: async () => {
            const newEpic = await epicService.create(epicPayload);
            recreatedEpicId = newEpic.id!;
          },
          onRedo: async () => {
            if (recreatedEpicId) {
              await epicService.delete(recreatedEpicId);
            } else {
              await epicService.delete(variables.id);
            }
          },
          queryKeyToInvalidate: ['epics'],
        });
      } catch (error) {
        console.error(error);
      }

      queryClient.invalidateQueries({ queryKey: ['epics'] });
      toast.success('Epic deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to delete epic');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Record<string, any> }) =>
      epicService.update(id, data),
    onSuccess: async (_updated, variables) => {
      const previous = editingEpic;
      if (previous) {
        try {
          await trackMutation({
            description: `Updated epic "${previous?.name}"`,
            entityType: 'epic',
            entityId: variables.id,
            previousState: previous,
            newState: { ...previous, ...variables.data },
            onUndo: async () => {
              const payload: Record<string, any> = {
                name: previous?.name,
                projectId: previous?.projectId ?? previous?.project?.id,
                managerId: previous?.managerId ?? previous?.manager?.id ?? user?.id,
              };
              if (previous?.deliverables || previous?.description) {
                payload.deliverables = previous?.deliverables || previous?.description;
              }
              if (previous?.isStart || previous?.estimatedStartDate) {
                payload.isStart = previous?.isStart || previous?.estimatedStartDate;
              }
              if (previous?.isEnd || previous?.estimatedEndDate) {
                payload.isEnd = previous?.isEnd || previous?.estimatedEndDate;
              }
              await epicService.update(variables.id, payload);
            },
            onRedo: async () => {
              await epicService.update(variables.id, variables.data);
            },
            queryKeyToInvalidate: ['epics'],
          });
        } catch (error) {
          console.error(error);
        }
      }

      queryClient.invalidateQueries({ queryKey: ['epics'] });
      toast.success('Epic updated successfully');
      setIsModalOpen(false);
      setEditingEpic(null);
      resetForm();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to update epic');
    },
  });

  const downloadMutation = useMutation({
    mutationFn: (managerId: number) => epicService.downloadManagerEpics(managerId),
    onSuccess: () => {
      toast.success('Epics document downloaded successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to download epics document');
    },
  });

  // Handlers
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validate required fields
    if (!formData.name?.trim()) {
      toast.error('Epic name is required');
      return;
    }
    if (!formData.projectId || formData.projectId === 0) {
      toast.error('Please select a project');
      return;
    }
    if (!formData.managerId || formData.managerId === 0) {
      toast.error('Please select a manager');
      return;
    }

    // Prepare data with only backend-known fields
    const submitData: Record<string, any> = {
      name: formData.name.trim(),
      projectId: Number(formData.projectId),
      managerId: Number(formData.managerId),
    };
    // Optional backend fields
    if (formData.description?.trim()) submitData.deliverables = formData.description.trim();
    if (formData.estimatedStartDate) submitData.isStart = formData.estimatedStartDate;
    if (formData.estimatedEndDate) submitData.isEnd = formData.estimatedEndDate;

    if (editingEpic?.id || editingEpic?.EpicId || editingEpic?.epicId) {
      const epicId = editingEpic?.id || editingEpic?.EpicId || editingEpic?.epicId;
      updateMutation.mutate({ id: epicId, data: submitData });
    } else {
      createMutation.mutate(submitData as any);
    }
  };

  const handleDelete = (id: number) => {
    const epic = epics.find((e: any) => e.id === id || e.epicId === id || e.EpicId === id);
    if (window.confirm('Are you sure you want to delete this epic?')) {
      deleteMutation.mutate({ id, epic });
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      description: '',
      projectId: 0 as any,
      managerId: user?.id || 0,
      assignedToUserId: 0,
      estimatedStartDate: '',
      estimatedEndDate: '',
    });
    setEditingEpic(null);
  };

  const handleOpenModal = () => {
    resetForm();
    setIsModalOpen(true);
  };

  const handleEdit = (epic: any) => {
    setEditingEpic(epic);
    setFormData({
      name: epic?.name || '',
      description: epic?.deliverables || epic?.description || '',
      projectId: epic?.projectId ?? epic?.project?.id ?? 0,
      managerId: (epic?.managerId ?? epic?.manager?.id ?? user?.id) || 0,
      assignedToUserId: epic?.assignedToUserId ?? epic?.assignedToUser?.id ?? 0,
      estimatedStartDate: epic?.isStart || epic?.estimatedStartDate || '',
      estimatedEndDate: epic?.isEnd || epic?.estimatedEndDate || '',
    });
    setIsModalOpen(true);
  };

  // Filter epics
  const filteredEpics = epics.filter((epic: any) => {
    const matchesSearch = epic.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesProject = !filterProject || epic.project?.id === Number(filterProject);
    return matchesSearch && matchesProject;
  });

  if (isLoading) {
    return <Loader />;
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">Epics</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">Manage epics across all projects</p>
        </div>
        <div className="flex gap-2">
          {hasAccessLevel(2) && user?.id && (
            <Button
              variant="secondary"
              icon={<Download className="w-4 h-4" />}
                onClick={() => downloadMutation.mutate(user?.id!)}
              isLoading={downloadMutation.isPending}
            >
              Download My Epics
            </Button>
          )}
          {hasAccessLevel(2) && (
            <Button onClick={handleOpenModal} icon={<Plus className="w-4 h-4" />}>
              Create Epic
            </Button>
          )}
        </div>
      </div>

      {/* Filters */}
      <Card className="p-4">
        <div className="flex gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
            <Input
              placeholder="Search epics..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          <Select
            value={filterProject}
            onChange={(e) => setFilterProject(e.target.value)}
            className="w-64"
          >
            <option value="">All Projects</option>
            {projects.map((project) => (
              <option key={project.projectId || project.id} value={project.projectId || project.id}>
                {project.name}
              </option>
            ))}
          </Select>
        </div>
      </Card>

      {/* Epics List */}
      {filteredEpics.length === 0 ? (
        <Card className="p-12 text-center">
          <AlertCircle className="w-12 h-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">No epics found</h3>
          <p className="text-gray-600 dark:text-gray-400 mb-4">
            {searchTerm || filterProject
              ? 'Try adjusting your filters'
              : 'Create your first epic to get started'}
          </p>
          {hasAccessLevel(2) && !searchTerm && !filterProject && (
            <Button onClick={handleOpenModal} variant="secondary">
              Create First Epic
            </Button>
          )}
        </Card>
      ) : (
        <div className="grid gap-4">
          {filteredEpics.map((epic, index) => (
            <motion.div
              key={epic.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.05 }}
            >
              <Card className="p-6 hover:shadow-lg transition-shadow">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="text-xl font-semibold text-gray-900 dark:text-gray-100">{epic.name}</h3>
                      <Badge variant="info">{epic.project?.name || 'No Project'}</Badge>
                      {(epic as any).isEnd ? (
                        <Badge variant="success">
                          <CheckCircle2 className="w-3 h-3 mr-1 inline" />
                          Completed
                        </Badge>
                      ) : (epic as any).isApproved ? (
                        <Badge variant="warning">Approved</Badge>
                      ) : (
                        <Badge variant="gray">In Progress</Badge>
                      )}
                    </div>
                    <p className="text-gray-600 dark:text-gray-400 mb-4">{epic.description || 'No description'}</p>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                      <div>
                        <span className="text-gray-500 dark:text-gray-400">Assigned To:</span>
                        <p className="font-medium text-gray-900 dark:text-gray-100">
                          {epic.assignedToUser
                            ? `${epic.assignedToUser.firstName} ${epic.assignedToUser.lastName}`
                            : 'Unassigned'}
                        </p>
                      </div>
                      <div>
                        <span className="text-gray-500 dark:text-gray-400">Start Date:</span>
                        <p className="font-medium text-gray-900 dark:text-gray-100">
                          {epic.estimatedStartDate ? formatDate(epic.estimatedStartDate) : 'Not set'}
                        </p>
                      </div>
                      <div>
                        <span className="text-gray-500 dark:text-gray-400">End Date:</span>
                        <p className="font-medium text-gray-900 dark:text-gray-100">
                          {epic.estimatedEndDate ? formatDate(epic.estimatedEndDate) : 'Not set'}
                        </p>
                      </div>
                      <div>
                        <span className="text-gray-500 dark:text-gray-400">Created:</span>
                        <p className="font-medium text-gray-900 dark:text-gray-100">{formatDate(epic.createdAt)}</p>
                      </div>
                    </div>
                  </div>
                  {hasAccessLevel(2) && (
                    <div className="flex gap-2 ml-4">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleEdit(epic)}
                        icon={<Pencil className="w-4 h-4" />}
                      />
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDelete(epic.id!)}
                        icon={<Trash2 className="w-4 h-4" />}
                      />
                    </div>
                  )}
                </div>
              </Card>
            </motion.div>
          ))}
        </div>
      )}

      {/* Create Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          resetForm();
        }}
        title={editingEpic ? 'Edit Epic' : 'Create New Epic'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Epic Name *"
            placeholder="Enter epic name"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            required
          />

          <div className="space-y-2">
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">
              Description
            </label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              rows={4}
              placeholder="Enter epic description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>

          <Select
            label="Project *"
            value={formData.projectId || ''}
            onChange={(e) => setFormData({ ...formData, projectId: Number(e.target.value) })}
            required
          >
            <option value="">Select a project</option>
            {projects.map((project) => (
              <option key={project.projectId || project.id} value={project.projectId || project.id}>
                {project.name}
              </option>
            ))}
          </Select>

          <Select
            label="Manager *"
            value={formData.managerId || ''}
            onChange={(e) =>
              setFormData({ ...formData, managerId: Number(e.target.value) })
            }
            required
          >
            <option value="">Select a manager</option>
            {users.map((user) => (
              <option key={user.id} value={user.id}>
                {user.firstName} {user.lastName} ({user.email})
              </option>
            ))}
          </Select>

          <Select
            label="Assign To"
            value={formData.assignedToUserId}
            onChange={(e) =>
              setFormData({ ...formData, assignedToUserId: Number(e.target.value) })
            }
          >
            <option value={0}>Unassigned</option>
            {users.map((user) => (
              <option key={user.id} value={user.id}>
                {user.firstName} {user.lastName} ({user.email})
              </option>
            ))}
          </Select>

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Estimated Start Date"
              type="date"
              value={formData.estimatedStartDate}
              onChange={(e) =>
                setFormData({ ...formData, estimatedStartDate: e.target.value })
              }
            />
            <Input
              label="Estimated End Date"
              type="date"
              value={formData.estimatedEndDate}
              onChange={(e) =>
                setFormData({ ...formData, estimatedEndDate: e.target.value })
              }
            />
          </div>

          <div className="flex justify-end gap-3 pt-4">
            <Button
              type="button"
              variant="ghost"
              onClick={() => {
                setIsModalOpen(false);
                resetForm();
              }}
            >
              Cancel
            </Button>
            <Button type="submit" isLoading={createMutation.isPending || updateMutation.isPending}>
              {editingEpic ? 'Save Changes' : 'Create Epic'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
