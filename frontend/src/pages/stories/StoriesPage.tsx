import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Search, Trash2, Download, ChevronDown, ChevronUp, Pencil, CheckCircle } from 'lucide-react';
import { Card, CardBody } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { Loader } from '../../components/ui/Loader';
import { Modal, ModalFooter } from '../../components/ui/Modal';
import { storyService } from '../../services/storyService';
import { projectService } from '../../services/projectService';
import { userService } from '../../services/userService';
import { epicService } from '../../services/epicService';
import { useAuthStore } from '../../store/authStore';
import { useUndoRedo } from '../../hooks/useUndoRedo';
import { formatDate } from '../../utils/helpers';
import toast from 'react-hot-toast';
import type { StoryFormData } from '../../types';
import { Select } from '../../components/ui/Select';
import { TimeTracking } from '../../components/ui/TimeTracking';

export const StoriesPage: React.FC = () => {
  const queryClient = useQueryClient();
  const { user, hasAccessLevel } = useAuthStore();
  const { trackMutation } = useUndoRedo();
  
  const [searchTerm, setSearchTerm] = useState('');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [expandedStories, setExpandedStories] = useState<Record<number, boolean>>({});
  const [editingStory, setEditingStory] = useState<any | null>(null);
  const [formData, setFormData] = useState<StoryFormData>({
    title: '',
    projectId: 0,
    assignedToId: user?.id || 0,
    description: '',
    deliverables: '',
    dueDate: '',
    workflowStateId: 0,
  });

  // Handle epic selection and auto-populate project
  const handleEpicChange = (epicId: number) => {
    const selectedEpic = epics?.find((e: any) => e.EpicId === epicId || e.epicId === epicId || e.id === epicId);
    if (selectedEpic) {
      setFormData({ 
        ...formData, 
        workflowStateId: epicId,
        projectId: selectedEpic.projectId || selectedEpic.project?.id || 0
      });
    } else {
      setFormData({ ...formData, workflowStateId: epicId });
    }
  };

  // Fetch data
  const { hasHydrated } = useAuthStore();
  
  const { data: stories, isLoading } = useQuery({
    queryKey: ['stories'],
    queryFn: storyService.getAll,
    enabled: hasHydrated,
  });

  const { data: projects } = useQuery({
    queryKey: ['projects'],
    queryFn: projectService.getAll,
    enabled: hasHydrated,
  });

  const { data: users } = useQuery({
    queryKey: ['users'],
    queryFn: userService.getAll,
    enabled: hasHydrated,
  });

  const { data: epics } = useQuery({
    queryKey: ['epics'],
    queryFn: epicService.getAll,
    enabled: hasHydrated,
  });

  // Create mutation
  const createMutation = useMutation({
    mutationFn: storyService.create,
    onSuccess: async (newStory) => {
      const previousData = queryClient.getQueryData(['stories']) as any[];
      
      try {
        await trackMutation({
          description: `Created story "${newStory.name}"`,
          entityType: 'story',
          entityId: newStory.id!,
          newState: newStory,
          onUndo: async () => {
            // Delete the story on undo
            await storyService.delete(newStory.id!);
          },
          onRedo: async () => {
            // Recreate the story on redo
            await storyService.create(newStory);
          },
          queryKeyToInvalidate: ['stories'],
        });
      } catch (error) {
        console.error(error);
      }

      queryClient.invalidateQueries({ queryKey: ['stories'] });
      toast.success('Story created successfully!');
      setIsCreateModalOpen(false);
      resetForm();
    },
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: ({ id }: { id: number; story: any }) => storyService.delete(id),
    onSuccess: async (_data, variables) => {
      const story = variables.story;
      let recreatedStoryId: number | null = null;
      
      const storyPayload = {
        title: story?.title,
        projectId: story?.projectId ?? story?.project?.id,
        assignedToId: story?.assignedToId ?? story?.assigned_to?.id,
        description: story?.description ?? story?.deliverables,
        deliverables: story?.deliverables,
        dueDate: story?.dueDate,
        workflowStateId: story?.workflowStateId ?? story?.EpicId ?? story?.epicId,
      };

      try {
        await trackMutation({
          description: `Deleted story "${story?.title}"`,
          entityType: 'story',
          entityId: variables.id,
          previousState: story,
          onUndo: async () => {
            const newStory = await storyService.create(storyPayload);
            recreatedStoryId = newStory.storyId || newStory.id;
          },
          onRedo: async () => {
            if (recreatedStoryId) {
              await storyService.delete(recreatedStoryId);
            } else {
              await storyService.delete(variables.id);
            }
          },
          queryKeyToInvalidate: ['stories'],
        });
      } catch (error) {
        console.error(error);
      }

      queryClient.invalidateQueries({ queryKey: ['stories'] });
      toast.success('Story deleted successfully!');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<StoryFormData> }) => storyService.update(id, data),
    onSuccess: async (_updated, variables) => {
      const previous = editingStory;
      if (previous) {
        try {
          await trackMutation({
            description: `Updated story "${previous?.title}"`,
            entityType: 'story',
            entityId: variables.id,
            previousState: previous,
            newState: { ...previous, ...variables.data },
            onUndo: async () => {
              const payload: Partial<StoryFormData> = {
                title: previous?.title,
                projectId: previous?.projectId ?? previous?.project?.id,
                assignedToId: previous?.assignedToId ?? previous?.assigned_to?.id,
                description: previous?.description ?? previous?.deliverables,
                deliverables: previous?.deliverables,
                dueDate: previous?.dueDate,
                workflowStateId: previous?.workflowStateId ?? previous?.EpicId ?? previous?.epicId,
              };
              await storyService.update(variables.id, payload);
            },
            onRedo: async () => {
              await storyService.update(variables.id, variables.data);
            },
            queryKeyToInvalidate: ['stories'],
          });
        } catch (error) {
          console.error(error);
        }
      }

      queryClient.invalidateQueries({ queryKey: ['stories'] });
      toast.success('Story updated successfully!');
      setIsCreateModalOpen(false);
      setEditingStory(null);
      resetForm();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to update story');
    },
  });

  const downloadMutation = useMutation({
    mutationFn: (managerId: number) => storyService.downloadManagerStories(managerId),
    onSuccess: () => {
      toast.success('Stories document downloaded successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to download stories document');
    },
  });

  const completeMutation = useMutation({
    mutationFn: (id: number) => storyService.complete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['stories'] });
      queryClient.invalidateQueries({ queryKey: ['epics'] });
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      toast.success('Story marked as completed!');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to complete story');
    },
  });

  const resetForm = () => {
    setFormData({
      title: '',
      projectId: 0,
      assignedToId: user?.id || 0,
      description: '',
      deliverables: '',
      dueDate: '',
      workflowStateId: 0,
    });
    setEditingStory(null);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.title || !formData.projectId || !formData.workflowStateId) {
      toast.error('Please fill in all required fields including Epic');
      return;
    }
    if (editingStory?.storyId || editingStory?.id || editingStory?.StoryId) {
      const storyId = editingStory?.storyId || editingStory?.id || editingStory?.StoryId;
      updateMutation.mutate({ id: storyId, data: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleEdit = (story: any) => {
    setEditingStory(story);
    setFormData({
      title: story?.title || '',
      projectId: story?.projectId ?? story?.project?.id ?? 0,
      assignedToId: (story?.assignedToId ?? story?.assigned_to?.id ?? user?.id) || 0,
      description: story?.description ?? story?.deliverables ?? '',
      deliverables: story?.deliverables || '',
      dueDate: story?.dueDate || '',
      workflowStateId: story?.workflowStateId ?? story?.EpicId ?? story?.epicId ?? 0,
    });
    setIsCreateModalOpen(true);
  };

  const toggleTimeTracking = (id: number) => {
    setExpandedStories((prev) => ({ ...prev, [id]: !prev[id] }));
  };

  // Filter stories
  const filteredStories = stories?.filter(story =>
    story.title.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const canCreate = hasAccessLevel(2);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-96">
        <Loader size="lg" text="Getting your stories ready..." />
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gradient">Stories</h1>
          <p className="text-slate-600 dark:text-gray-400 mt-1">Manage tasks and work items</p>
        </div>
        <div className="flex gap-2">
          {hasAccessLevel(2) && user?.id && (
            <Button
              variant="secondary"
              icon={<Download className="w-5 h-5" />}
              onClick={() => downloadMutation.mutate(user.id)}
              isLoading={downloadMutation.isPending}
            >
              Download My Stories
            </Button>
          )}
          {canCreate && (
            <Button
              variant="primary"
              icon={<Plus className="w-5 h-5" />}
              onClick={() => setIsCreateModalOpen(true)}
            >
              New Story
            </Button>
          )}
        </div>
      </div>

      {/* Filters */}
      <Card glass>
        <CardBody>
          <Input
            placeholder="Search stories..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            icon={<Search className="w-5 h-5" />}
          />
        </CardBody>
      </Card>

      {/* Stories List */}
      {filteredStories && filteredStories.length > 0 ? (
        <div className="grid grid-cols-1 gap-4">
          {filteredStories.map((story) => {
            const storyId = (story as any).storyId ?? (story as any).id ?? (story as any).StoryId;
            const assignedUser = users?.find((u) => u.id === (story as any).assignedToId);
            const isExpanded = !!expandedStories[storyId];
            return (
            <Card key={storyId ?? story.title} glass>
              <CardBody>
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="text-lg font-semibold text-slate-900 dark:text-gray-100">
                        {story.title}
                      </h3>
                      <Badge variant={story.isApproved ? 'success' : 'warning'}>
                        {story.isApproved ? 'Completed' : 'In Progress'}
                      </Badge>
                    </div>
                    {story.description && (
                      <p className="text-sm text-slate-600 dark:text-gray-400 mb-3">{story.description}</p>
                    )}
                    <div className="flex items-center gap-4 text-sm text-slate-500 dark:text-gray-400">
                      <span>Due: {formatDate(story.dueDate)}</span>
                      {assignedUser && (
                        <span>Assigned: {assignedUser.firstName} {assignedUser.lastName}</span>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button
                      size="sm"
                      variant="secondary"
                      icon={isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                      onClick={() => toggleTimeTracking(storyId as number)}
                    >
                      Time Logs
                    </Button>
                    {canCreate && !story.isApproved && (
                      <Button
                        size="sm"
                        variant="primary"
                        icon={<CheckCircle className="w-4 h-4" />}
                        onClick={() => {
                          if (window.confirm('Mark this story as completed?')) {
                            completeMutation.mutate(storyId as number);
                          }
                        }}
                        isLoading={completeMutation.isPending}
                      >
                        Complete
                      </Button>
                    )}
                    {canCreate && (
                      <Button
                        size="sm"
                        variant="secondary"
                        icon={<Pencil className="w-4 h-4" />}
                        onClick={() => handleEdit(story)}
                      >
                        Edit
                      </Button>
                    )}
                    {canCreate && (
                      <Button
                        size="sm"
                        variant="danger"
                        icon={<Trash2 className="w-4 h-4" />}
                        onClick={() => {
                          if (window.confirm('Delete this story?')) {
                            deleteMutation.mutate({ id: storyId as number, story });
                          }
                        }}
                      >
                        Delete
                      </Button>
                    )}
                  </div>
                </div>
                {isExpanded && storyId && (
                  <div className="mt-6">
                    <TimeTracking storyId={Number(storyId)} />
                  </div>
                )}
              </CardBody>
            </Card>
          );
          })}
        </div>
      ) : (
        <Card glass>
          <CardBody>
            <div className="text-center py-12">
              <p className="text-slate-600 dark:text-gray-400 mb-4">
                {searchTerm
                  ? `No stories match "${searchTerm}". Try a different search term.`
                  : 'No stories found. Create your first story to get started!'}
              </p>
              {canCreate && !searchTerm && (
                <Button
                  variant="primary"
                  onClick={() => setIsCreateModalOpen(true)}
                >
                  Create First Story
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
        title={editingStory ? 'Edit Story' : 'Create New Story'}
        size="lg"
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <Select
            label="Epic *"
            value={formData.workflowStateId || ''}
            onChange={(e) => handleEpicChange(Number(e.target.value))}
            options={epics?.map(e => ({
              value: e.EpicId ?? e.epicId ?? e.id,
              label: `${e.name} (Project: ${e.project?.name || 'N/A'})`
            })) || []}
            required
          />

          <Input
            label="Title"
            value={formData.title}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            required
          />

          <Select
            label="Project"
            value={formData.projectId}
            onChange={(e) => setFormData({ ...formData, projectId: Number(e.target.value) })}
            options={projects?.map(p => ({ value: p.projectId ?? p.id, label: p.name })) || []}
            disabled
            required
          />

          <Select
            label="Assigned To"
            value={formData.assignedToId}
            onChange={(e) => setFormData({ ...formData, assignedToId: Number(e.target.value) })}
            options={users?.map(u => ({ value: u.id, label: u.username })) || []}
            required
          />

          <Input
            label="Due Date"
            type="date"
            value={formData.dueDate}
            onChange={(e) => setFormData({ ...formData, estimatedEndDate: e.target.value })}
          />

          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-gray-300 mb-2">
              Description
            </label>
            <textarea
              className="input min-h-[80px]"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>

          <ModalFooter>
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setIsCreateModalOpen(false);
                resetForm();
              }}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              isLoading={createMutation.isPending || updateMutation.isPending}
            >
              {editingStory ? 'Save Changes' : 'Create Story'}
            </Button>
          </ModalFooter>
        </form>
      </Modal>
    </div>
  );
};
