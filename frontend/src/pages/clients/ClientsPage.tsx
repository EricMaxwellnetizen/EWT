import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Search, Edit, Trash2, AlertCircle, Download, Building2, Mail, Phone, ChevronDown, ChevronRight } from 'lucide-react';
import { clientService } from '../../services/clientService';
import type { Client, ClientDTO } from '../../types';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { Modal } from '../../components/ui/Modal';
import { Input } from '../../components/ui/Input';
import { Loader } from '../../components/ui/Loader';
import { HierarchyView } from '../../components/ui/HierarchyView';
import { useAuthStore } from '../../store/authStore';
import { useUndoRedo } from '../../hooks/useUndoRedo';
import toast from 'react-hot-toast';
import { motion, AnimatePresence } from 'framer-motion';
import { formatDate } from '../../utils/helpers';

export const ClientsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const { hasAccessLevel, hasHydrated } = useAuthStore();
  const { trackMutation } = useUndoRedo();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingClient, setEditingClient] = useState<Client | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [expandedClientId, setExpandedClientId] = useState<number | null>(null);
  const [formData, setFormData] = useState<ClientDTO>({
    name: '',
    email: '',
    phoneNumber: '',
    address: '',
  });

  // Queries
  const { data: clients = [], isLoading } = useQuery({
    queryKey: ['clients'],
    queryFn: clientService.getAll,
    enabled: hasHydrated,
  });

  // Fetch hierarchy for expanded client
  const { data: hierarchyData, isLoading: isLoadingHierarchy } = useQuery({
    queryKey: ['client-hierarchy', expandedClientId],
    queryFn: () => clientService.getHierarchy(expandedClientId!),
    enabled: expandedClientId !== null && hasHydrated,
  });

  // Mutations
  const createMutation = useMutation({
    mutationFn: clientService.create,
    onSuccess: async (newClient) => {
      try {
        await trackMutation({
          description: `Created client "${newClient.name}"`,
          entityType: 'client',
          entityId: newClient.id!,
          newState: newClient,
          onUndo: async () => {
            await clientService.delete(newClient.id!);
          },
          onRedo: async () => {
            const payload: ClientDTO = {
              name: newClient.name,
              email: newClient.email,
              phoneNumber: newClient.phoneNumber || '',
              address: newClient.address || '',
            };
            await clientService.create(payload);
          },
          queryKeyToInvalidate: ['clients'],
        });
      } catch (error) {
        console.error(error);
      }

      queryClient.invalidateQueries({ queryKey: ['clients'] });
      toast.success('Client created successfully');
      setIsModalOpen(false);
      resetForm();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to create client');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: ClientDTO; previous?: Client }) =>
      clientService.update(id, data),
    onSuccess: async (_data, variables) => {
      const previous = variables.previous;
      if (previous) {
        const previousPayload: ClientDTO = {
          name: previous.name,
          email: previous.email,
          phoneNumber: previous.phoneNumber || '',
          address: previous.address || '',
        };

        try {
          await trackMutation({
            description: `Updated client "${previous.name}"`,
            entityType: 'client',
            entityId: variables.id,
            previousState: previous,
            newState: { ...previous, ...variables.data },
            onUndo: async () => {
              await clientService.update(variables.id, previousPayload);
            },
            onRedo: async () => {
              await clientService.update(variables.id, variables.data);
            },
            queryKeyToInvalidate: ['clients'],
          });
        } catch (error) {
          console.error(error);
        }
      }
      
      queryClient.invalidateQueries({ queryKey: ['clients'] });
      toast.success('Client updated successfully');
      setIsModalOpen(false);
      setEditingClient(null);
      resetForm();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to update client');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: ({ id }: { id: number; client: Client }) => clientService.delete(id),
    onSuccess: async (_data, variables) => {
      const client = variables.client;
      let recreatedClientId: number | null = null;
      
      const payload: ClientDTO = {
        name: client.name,
        email: client.email,
        phoneNumber: client.phoneNumber || '',
        address: client.address || '',
      };

      try {
        await trackMutation({
          description: `Deleted client "${client.name}"`,
          entityType: 'client',
          entityId: variables.id,
          previousState: client,
          onUndo: async () => {
            const newClient = await clientService.create(payload);
            recreatedClientId = newClient.id!;
          },
          onRedo: async () => {
            if (recreatedClientId) {
              await clientService.delete(recreatedClientId);
            } else {
              await clientService.delete(variables.id);
            }
          },
          queryKeyToInvalidate: ['clients'],
        });
      } catch (error) {
        console.error(error);
      }

      queryClient.invalidateQueries({ queryKey: ['clients'] });
      toast.success('Client deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to delete client');
    },
  });

  const downloadMutation = useMutation({
    mutationFn: clientService.downloadDocument,
    onSuccess: () => {
      toast.success('Clients document downloaded successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to download client document');
    },
  });

  // Handlers
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name || !formData.email) {
      toast.error('Please fill in all required fields');
      return;
    }

    if (editingClient) {
      updateMutation.mutate({ id: editingClient.id!, data: formData, previous: editingClient });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (id: number) => {
    const client = clients.find((c) => c.id === id);
    if (window.confirm('Are you sure you want to delete this client? This action cannot be undone.')) {
      if (client) {
        deleteMutation.mutate({ id, client });
      }
    }
  };

  const handleDownload = () => {
    downloadMutation.mutate();
  };

  const handleEdit = (client: Client) => {
    setEditingClient(client);
    setFormData({
      name: client.name,
      email: client.email,
      phoneNumber: client.phoneNumber || '',
      address: client.address || '',
    });
    setIsModalOpen(true);
  };

  const resetForm = () => {
    setFormData({
      name: '',
      email: '',
      phoneNumber: '',
      address: '',
    });
    setEditingClient(null);
  };

  const handleOpenModal = () => {
    resetForm();
    setIsModalOpen(true);
  };

  // Filter clients
  const filteredClients = clients.filter((client) =>
    client.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    client.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (isLoading) {
    return <Loader />;
  }

  // Only users with access level 2+ can manage clients
  const canManageClients = hasAccessLevel(2);
  const canDownloadClients = hasAccessLevel(3);

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Clients</h1>
          <p className="text-gray-600 mt-1">Manage client information and documents</p>
        </div>
        <div className="flex gap-2">
          {canDownloadClients && (
            <Button
              variant="secondary"
              icon={<Download className="w-4 h-4" />}
              onClick={handleDownload}
              isLoading={downloadMutation.isPending}
            >
              Download Clients
            </Button>
          )}
          {canManageClients && (
            <Button onClick={handleOpenModal} icon={<Plus className="w-4 h-4" />}>
              Add Client
            </Button>
          )}
        </div>
      </div>

      {/* Search */}
      <Card className="p-4">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          <Input
            placeholder="Search clients..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
      </Card>

      {/* Clients Grid */}
      {filteredClients.length === 0 ? (
        <Card className="p-12 text-center">
          <AlertCircle className="w-12 h-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">No clients found</h3>
          <p className="text-gray-600 mb-4">
            {searchTerm
              ? 'Try adjusting your search term'
              : 'Add your first client to get started'}
          </p>
          {canManageClients && !searchTerm && (
            <Button onClick={handleOpenModal} variant="secondary">
              Add First Client
            </Button>
          )}
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {filteredClients.map((client, index) => (
            <motion.div
              key={client.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.05 }}
              className="col-span-1"
            >
              <Card className="p-6 hover:shadow-lg transition-shadow">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-full bg-gradient-to-br from-secondary-500 to-secondary-600 flex items-center justify-center text-white">
                      <Building2 className="w-6 h-6" />
                    </div>
                    <div>
                      <h3 className="font-semibold text-gray-900">{client.name}</h3>
                      <p className="text-xs text-gray-500">
                        Added {formatDate(client.createdAt)}
                      </p>
                    </div>
                  </div>
                  {canManageClients && (
                    <div className="flex gap-1">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleEdit(client)}
                        icon={<Edit className="w-4 h-4" />}
                      />
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDelete(client.id!)}
                        icon={<Trash2 className="w-4 h-4" />}
                      />
                    </div>
                  )}
                </div>

                <div className="space-y-3">
                  <div className="flex items-start gap-2">
                    <Mail className="w-4 h-4 text-gray-400 mt-0.5 flex-shrink-0" />
                    <div className="flex-1 min-w-0">
                      <p className="text-sm text-gray-600 truncate">{client.email}</p>
                    </div>
                  </div>

                  {client.phoneNumber && (
                    <div className="flex items-start gap-2">
                      <Phone className="w-4 h-4 text-gray-400 mt-0.5 flex-shrink-0" />
                      <div className="flex-1">
                        <p className="text-sm text-gray-600">{client.phoneNumber}</p>
                      </div>
                    </div>
                  )}

                  {client.address && (
                    <div className="flex items-start gap-2">
                      <Building2 className="w-4 h-4 text-gray-400 mt-0.5 flex-shrink-0" />
                      <div className="flex-1">
                        <p className="text-sm text-gray-600">{client.address}</p>
                      </div>
                    </div>
                  )}

                  {/* Expand/Collapse Button */}
                  <div className="pt-2 border-t">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setExpandedClientId(expandedClientId === client.id ? null : client.id!)}
                      icon={expandedClientId === client.id ? <ChevronDown className="w-4 h-4" /> : <ChevronRight className="w-4 h-4" />}
                      className="w-full justify-start"
                    >
                      {expandedClientId === client.id ? 'Hide' : 'View'} Projects & Hierarchy
                    </Button>
                  </div>
                </div>

                {/* Hierarchical View */}
                <AnimatePresence>
                  {expandedClientId === client.id && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: 'auto', opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      transition={{ duration: 0.3 }}
                      className="mt-4 border-t pt-4"
                    >
                      {isLoadingHierarchy ? (
                        <div className="flex justify-center py-4">
                          <Loader />
                        </div>
                      ) : hierarchyData ? (
                        <div className="space-y-2">
                          {hierarchyData.projects.length === 0 ? (
                            <p className="text-sm text-gray-500 text-center py-4">No projects found for this client</p>
                          ) : (
                            hierarchyData.projects.map((project) => (
                              <HierarchyView
                                key={project.id}
                                type="project"
                                data={{
                                  id: project.id,
                                  name: project.name,
                                  type: 'project',
                                  description: project.description,
                                  deadline: project.deadline,
                                  isApproved: project.isApproved,
                                  manager: project.manager ? {
                                    id: project.manager.id,
                                    firstName: project.manager.firstName,
                                    lastName: project.manager.lastName,
                                    email: project.manager.email,
                                    role: project.manager.role,
                                  } : undefined,
                                  progress: project.progress || 0,
                                  epics: project.epics.map((epic) => ({
                                    id: epic.id,
                                    name: epic.name,
                                    type: 'epic' as const,
                                    description: epic.description,
                                    dueDate: epic.dueDate,
                                    isApproved: epic.isApproved,
                                    manager: epic.manager ? {
                                      id: epic.manager.id,
                                      firstName: epic.manager.firstName,
                                      lastName: epic.manager.lastName,
                                      email: epic.manager.email,
                                      role: epic.manager.role,
                                    } : undefined,
                                    progress: epic.progress || 0,
                                    stories: epic.stories.map((story) => ({
                                      id: story.id,
                                      name: story.name,
                                      type: 'story' as const,
                                      description: story.description,
                                      status: story.status,
                                      dueDate: story.dueDate,
                                      isApproved: story.isApproved,
                                      assigned_to: story.assignedTo ? {
                                        id: story.assignedTo.id,
                                        firstName: story.assignedTo.firstName,
                                        lastName: story.assignedTo.lastName,
                                        email: story.assignedTo.email,
                                        role: story.assignedTo.role,
                                      } : undefined,
                                    })),
                                  })),
                                }}
                              />
                            ))
                          )}
                        </div>
                      ) : null}
                    </motion.div>
                  )}
                </AnimatePresence>
              </Card>
            </motion.div>
          ))}
        </div>
      )}

      {/* Create/Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setEditingClient(null);
          resetForm();
        }}
        title={editingClient ? 'Edit Client' : 'Add New Client'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Client Name *"
            placeholder="Enter client name"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            required
          />

          <Input
            label="Email *"
            type="email"
            placeholder="client@example.com"
            value={formData.email}
            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            required
          />

          <Input
            label="Phone Number"
            type="tel"
            placeholder="(555) 123-4567"
            value={formData.phoneNumber}
            onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
          />

          <div className="space-y-2">
            <label className="block text-sm font-medium text-gray-700">
              Address
            </label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              rows={3}
              placeholder="Enter client address"
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
            />
          </div>

          <div className="flex justify-end gap-3 pt-4">
            <Button
              type="button"
              variant="ghost"
              onClick={() => {
                setIsModalOpen(false);
                setEditingClient(null);
                resetForm();
              }}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              isLoading={createMutation.isPending || updateMutation.isPending}
            >
              {editingClient ? 'Update Client' : 'Add Client'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
