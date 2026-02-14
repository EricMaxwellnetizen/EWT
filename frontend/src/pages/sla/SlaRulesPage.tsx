import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Search, Edit, Trash2, AlertCircle, Clock, AlertTriangle, Download } from 'lucide-react';
import { slaService } from '../../services/slaService';
import type { SlaRule, SlaRuleDTO } from '../../types';
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

export const SlaRulesPage: React.FC = () => {
  const queryClient = useQueryClient();
  const { hasAccessLevel, hasHydrated } = useAuthStore();
  const { trackMutation } = useUndoRedo();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingRule, setEditingRule] = useState<SlaRule | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [formData, setFormData] = useState<SlaRuleDTO>({
    name: '',
    description: '',
    targetEntityType: 'PROJECT',
    maxDurationDays: 0,
    notificationThresholdDays: 0,
  });

  // Queries
  const { data: rules = [], isLoading } = useQuery({
    queryKey: ['sla-rules'],
    queryFn: slaService.getAll,
    enabled: hasHydrated,
  });

  // Mutations
  const createMutation = useMutation({
    mutationFn: slaService.create,
    onSuccess: async (newRule) => {
      try {
        await trackMutation({
          description: `Created SLA rule "${newRule.name}"`,
          entityType: 'sla',
          entityId: newRule.id!,
          newState: newRule,
          onUndo: async () => {
            await slaService.delete(newRule.id!);
          },
          onRedo: async () => {
            const payload: SlaRuleDTO = {
              name: newRule.name,
              description: newRule.description || '',
              targetEntityType: newRule.targetEntityType,
              maxDurationDays: newRule.maxDurationDays,
              notificationThresholdDays: newRule.notificationThresholdDays,
            };
            await slaService.create(payload);
          },
          queryKeyToInvalidate: ['sla-rules'],
        });
      } catch (error) {
        console.error(error);
      }

      queryClient.invalidateQueries({ queryKey: ['sla-rules'] });
      toast.success('SLA rule created successfully');
      setIsModalOpen(false);
      resetForm();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to create SLA rule');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: SlaRuleDTO }) =>
      slaService.update(id, data),
    onSuccess: async (_data, variables) => {
      const rule = rules.find(r => r.id === variables.id);
      
      if (rule) {
        const previousPayload: SlaRuleDTO = {
          name: rule.name,
          description: rule.description || '',
          targetEntityType: rule.targetEntityType,
          maxDurationDays: rule.maxDurationDays,
          notificationThresholdDays: rule.notificationThresholdDays,
        };

        trackMutation({
          description: `Updated SLA rule "${rule.name}"`,
          entityType: 'sla',
          entityId: variables.id,
          previousState: rule,
          newState: { ...rule, ...variables.data },
          onUndo: async () => {
            await slaService.update(variables.id, previousPayload);
          },
          onRedo: async () => {
            await slaService.update(variables.id, variables.data);
          },
          queryKeyToInvalidate: ['sla-rules'],
        }).catch(console.error);
      }

      queryClient.invalidateQueries({ queryKey: ['sla-rules'] });
      toast.success('SLA rule updated successfully');
      setIsModalOpen(false);
      setEditingRule(null);
      resetForm();
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to update SLA rule');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => slaService.delete(id),
    onSuccess: async (_data, ruleId) => {
      const rule = rules.find(r => r.id === ruleId);
      
      if (rule) {
        const rulePayload: SlaRuleDTO = {
          name: rule.name,
          description: rule.description || '',
          targetEntityType: rule.targetEntityType,
          maxDurationDays: rule.maxDurationDays,
          notificationThresholdDays: rule.notificationThresholdDays,
        };

        let recreatedRuleId: number | null = null;

        trackMutation({
          description: `Deleted SLA rule "${rule.name}"`,
          entityType: 'sla',
          entityId: ruleId,
          previousState: rule,
          onUndo: async () => {
            const newRule = await slaService.create(rulePayload);
            recreatedRuleId = newRule.id!;
          },
          onRedo: async () => {
            if (recreatedRuleId) {
              await slaService.delete(recreatedRuleId);
            } else {
              await slaService.delete(ruleId);
            }
          },
          queryKeyToInvalidate: ['sla-rules'],
        }).catch(console.error);
      }

      queryClient.invalidateQueries({ queryKey: ['sla-rules'] });
      toast.success('SLA rule deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to delete SLA rule');
    },
  });

  const downloadMutation = useMutation({
    mutationFn: slaService.downloadDocument,
    onSuccess: () => {
      toast.success('SLA rules document downloaded successfully');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to download SLA rules document');
    },
  });

  // Handlers
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name || !formData.maxDurationDays || !formData.notificationThresholdDays) {
      toast.error('Please fill in all required fields');
      return;
    }

    if (formData.notificationThresholdDays >= formData.maxDurationDays) {
      toast.error('Notification threshold must be less than max duration');
      return;
    }

    if (editingRule) {
      updateMutation.mutate({ id: editingRule.id!, data: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (id: number) => {
    if (window.confirm('Are you sure you want to delete this SLA rule? This action cannot be undone.')) {
      deleteMutation.mutate(id);
    }
  };

  const handleEdit = (rule: SlaRule) => {
    setEditingRule(rule);
    setFormData({
      name: rule.name,
      description: rule.description || '',
      targetEntityType: rule.targetEntityType,
      maxDurationDays: rule.maxDurationDays,
      notificationThresholdDays: rule.notificationThresholdDays,
    });
    setIsModalOpen(true);
  };

  const resetForm = () => {
    setFormData({
      name: '',
      description: '',
      targetEntityType: 'PROJECT',
      maxDurationDays: 0,
      notificationThresholdDays: 0,
    });
    setEditingRule(null);
  };

  const handleOpenModal = () => {
    resetForm();
    setIsModalOpen(true);
  };

  const getEntityTypeBadgeVariant = (type: string) => {
    switch (type) {
      case 'PROJECT':
        return 'info';
      case 'EPIC':
        return 'warning';
      case 'STORY':
        return 'success';
      default:
        return 'secondary';
    }
  };

  // Filter rules
  const filteredRules = rules.filter((rule) =>
    rule.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (isLoading) {
    return <Loader />;
  }

  // Only users with access level 4+ can manage SLA rules
  const canManageSlaRules = hasAccessLevel(4);

  if (!canManageSlaRules) {
    return (
      <div className="p-6">
        <Card className="p-12 text-center">
          <AlertTriangle className="w-16 h-16 text-yellow-500 mx-auto mb-4" />
          <h3 className="text-2xl font-semibold text-gray-900 mb-2">Access Denied</h3>
          <p className="text-gray-600">
            You need Senior Manager or Admin level access to view and manage SLA rules.
          </p>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">SLA Rules</h1>
          <p className="text-gray-600 mt-1">Define service level agreement rules and thresholds</p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="secondary"
            icon={<Download className="w-4 h-4" />}
            onClick={() => downloadMutation.mutate()}
            isLoading={downloadMutation.isPending}
          >
            Download SLA Rules
          </Button>
          <Button onClick={handleOpenModal} icon={<Plus className="w-4 h-4" />}>
            Create SLA Rule
          </Button>
        </div>
      </div>

      {/* Info Card */}
      <Card className="p-4 bg-blue-50 border-blue-200">
        <div className="flex items-start gap-3">
          <AlertCircle className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" />
          <div className="flex-1">
            <h4 className="font-semibold text-blue-900 mb-1">About SLA Rules</h4>
            <p className="text-sm text-blue-800">
              SLA rules define maximum duration and notification thresholds for projects, epics, and stories.
              The system will automatically monitor these rules and send notifications when thresholds are reached.
            </p>
          </div>
        </div>
      </Card>

      {/* Search */}
      <Card className="p-4">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          <Input
            placeholder="Search SLA rules..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
      </Card>

      {/* Rules List */}
      {filteredRules.length === 0 ? (
        <Card className="p-12 text-center">
          <AlertCircle className="w-12 h-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-gray-900 mb-2">No SLA rules found</h3>
          <p className="text-gray-600 mb-4">
            {searchTerm
              ? 'Try adjusting your search term'
              : 'Create your first SLA rule to start monitoring service levels'}
          </p>
          {!searchTerm && (
            <Button onClick={handleOpenModal} variant="secondary">
              Create First SLA Rule
            </Button>
          )}
        </Card>
      ) : (
        <div className="grid gap-4">
          {filteredRules.map((rule, index) => (
            <motion.div
              key={rule.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.05 }}
            >
              <Card className="p-6 hover:shadow-lg transition-shadow">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="text-xl font-semibold text-gray-900">{rule.name}</h3>
                      <Badge variant="success">
                        {rule.targetEntityType}
                      </Badge>
                    </div>
                    <p className="text-gray-600">{rule.description || 'No description'}</p>
                  </div>
                  <div className="flex gap-2 ml-4">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleEdit(rule)}
                      icon={<Edit className="w-4 h-4" />}
                    />
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => handleDelete(rule.id!)}
                      icon={<Trash2 className="w-4 h-4" />}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                    <div className="p-2 bg-primary-100 rounded-lg">
                      <Clock className="w-5 h-5 text-primary-600" />
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">Max Duration</p>
                      <p className="text-lg font-semibold text-gray-900">
                        {rule.maxDurationDays} days
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                    <div className="p-2 bg-yellow-100 rounded-lg">
                      <AlertTriangle className="w-5 h-5 text-yellow-600" />
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">Notification At</p>
                      <p className="text-lg font-semibold text-gray-900">
                        {rule.notificationThresholdDays} days
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                    <div>
                      <p className="text-xs text-gray-500">Created</p>
                      <p className="text-sm font-medium text-gray-900">
                        {formatDate(rule.createdAt)}
                      </p>
                    </div>
                  </div>
                </div>
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
          setEditingRule(null);
          resetForm();
        }}
        title={editingRule ? 'Edit SLA Rule' : 'Create New SLA Rule'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Rule Name *"
            placeholder="Enter rule name"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            required
          />

          <div className="space-y-2">
            <label className="block text-sm font-medium text-gray-700">
              Description
            </label>
            <textarea
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              rows={3}
              placeholder="Enter rule description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            />
          </div>

          <Select
            label="Target Entity Type *"
            value={formData.targetEntityType}
            onChange={(e) => setFormData({ ...formData, targetEntityType: e.target.value as any })}
            required
          >
            <option value="PROJECT">Project</option>
            <option value="EPIC">Epic</option>
            <option value="STORY">Story</option>
          </Select>

          <Input
            label="Maximum Duration (days) *"
            type="number"
            min="1"
            placeholder="Enter max duration in days"
            value={formData.maxDurationDays}
            onChange={(e) => setFormData({ ...formData, maxDurationDays: Number(e.target.value) })}
            required
          />

          <Input
            label="Notification Threshold (days) *"
            type="number"
            min="1"
            placeholder="Enter notification threshold in days"
            value={formData.notificationThresholdDays}
            onChange={(e) =>
              setFormData({ ...formData, notificationThresholdDays: Number(e.target.value) })
            }
            required
          />

          {formData.maxDurationDays > 0 && formData.notificationThresholdDays > 0 && (
            <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg">
              <p className="text-sm text-blue-800">
                Notifications will be sent when a {formData.targetEntityType.toLowerCase()} reaches{' '}
                <strong>{formData.notificationThresholdDays} days</strong>, with a maximum allowed
                duration of <strong>{formData.maxDurationDays} days</strong>.
              </p>
            </div>
          )}

          <div className="flex justify-end gap-3 pt-4">
            <Button
              type="button"
              variant="ghost"
              onClick={() => {
                setIsModalOpen(false);
                setEditingRule(null);
                resetForm();
              }}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              isLoading={createMutation.isPending || updateMutation.isPending}
            >
              {editingRule ? 'Update Rule' : 'Create Rule'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
