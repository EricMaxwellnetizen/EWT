import React, { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Bell, Check, Trash2, RefreshCw } from 'lucide-react';
import { notificationService } from '../../services/notificationService';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { useAuthStore } from '../../store/authStore';
import toast from 'react-hot-toast';

const playWhooshSound = () => {
  try {
    const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)();
    const duration = 0.35;
    const now = audioContext.currentTime;

    const bufferSize = audioContext.sampleRate * duration;
    const noiseBuffer = audioContext.createBuffer(1, bufferSize, audioContext.sampleRate);
    const noiseData = noiseBuffer.getChannelData(0);
    for (let i = 0; i < bufferSize; i++) {
      noiseData[i] = Math.random() * 2 - 1;
    }
    const noiseSource = audioContext.createBufferSource();
    noiseSource.buffer = noiseBuffer;

    const bandpass = audioContext.createBiquadFilter();
    bandpass.type = 'bandpass';
    bandpass.Q.value = 0.8;
    bandpass.frequency.setValueAtTime(2500, now);
    bandpass.frequency.exponentialRampToValueAtTime(300, now + duration);

    const gainNode = audioContext.createGain();
    gainNode.gain.setValueAtTime(0, now);
    gainNode.gain.linearRampToValueAtTime(0.25, now + 0.04);
    gainNode.gain.exponentialRampToValueAtTime(0.001, now + duration);

    noiseSource.connect(bandpass);
    bandpass.connect(gainNode);
    gainNode.connect(audioContext.destination);

    noiseSource.start(now);
    noiseSource.stop(now + duration);
  } catch (error) {
    console.log('Could not play whoosh sound:', error);
  }
};

export const NotificationsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const { user, hasAccessLevel, hasHydrated } = useAuthStore();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [cleanupUserId, setCleanupUserId] = useState<number>(user?.id || 0);
  const [createForm, setCreateForm] = useState({
    recipient: '',
    subject: 'SYSTEM_ALERT',
    message: '',
    templateId: '',
    priority: '',
  });
  const [batchPayload, setBatchPayload] = useState('[\n  {\n    "subject": "SYSTEM_ALERT",\n    "message": "...",\n    "templateId": "project",\n    "priority": "123"\n  }\n]');

  const { data, isLoading, refetch } = useQuery({
    queryKey: ['notifications', 'paginated', page, size],
    queryFn: () => notificationService.getPaginatedNotifications({ page, size }),    enabled: hasHydrated,  });

  const notifications = data?.content || [];
  const totalPages = data?.totalPages || 1;

  const markAsReadMutation = useMutation({
    mutationFn: notificationService.markAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotificationCount'] });
    },
  });

  const markAllReadMutation = useMutation({
    mutationFn: notificationService.markAllAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotificationCount'] });
      toast.success('All notifications marked as read');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: notificationService.deleteNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotificationCount'] });
      playWhooshSound();
      toast.success('Notification deleted');
    },
  });

  const cleanupMutation = useMutation({
    mutationFn: (targetUserId: number) => notificationService.cleanupNotifications(targetUserId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotificationCount'] });
      playWhooshSound();
      toast.success('Old notifications cleaned up');
    },
  });

  const createMutation = useMutation({
    mutationFn: notificationService.createNotification,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotificationCount'] });
      toast.success('Notification created');
      setCreateForm({ recipient: '', subject: 'SYSTEM_ALERT', message: '', templateId: '', priority: '' });
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to create notification');
    },
  });

  const createBatchMutation = useMutation({
    mutationFn: notificationService.createNotificationsBatch,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotificationCount'] });
      toast.success('Batch notifications created');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to create batch notifications');
    },
  });

  const handleCreateNotification = () => {
    if (!createForm.message.trim()) {
      toast.error('Message is required');
      return;
    }
    createMutation.mutate({
      recipient: createForm.recipient || undefined,
      subject: createForm.subject || undefined,
      message: createForm.message,
      templateId: createForm.templateId || undefined,
      priority: createForm.priority || undefined,
    });
  };

  const handleCreateBatch = () => {
    try {
      const parsed = JSON.parse(batchPayload);
      if (!Array.isArray(parsed)) {
        toast.error('Batch payload must be an array');
        return;
      }
      createBatchMutation.mutate({ notifications: parsed });
    } catch (error) {
      toast.error('Invalid JSON for batch payload');
    }
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">Notifications</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">All activity updates for your account</p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="secondary"
            icon={<RefreshCw className="w-4 h-4" />}
            onClick={() => refetch()}
            isLoading={isLoading}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            icon={<Check className="w-4 h-4" />}
            onClick={() => markAllReadMutation.mutate()}
            isLoading={markAllReadMutation.isPending}
          >
            Mark All Read
          </Button>
        </div>
      </div>

      <Card className="p-4">
        {isLoading ? (
          <div className="py-8 text-center text-gray-500 dark:text-gray-400">Loading notifications...</div>
        ) : notifications.length === 0 ? (
          <div className="py-8 text-center text-gray-500 dark:text-gray-400">
            <Bell className="w-10 h-10 text-gray-300 dark:text-gray-600 mx-auto mb-2" />
            No notifications yet
          </div>
        ) : (
          <div className="divide-y divide-gray-100 dark:divide-gray-700">
            {notifications.map((notification) => (
              <div
                key={notification.id}
                className={`p-4 flex items-start gap-4 ${
                  !notification.isRead ? 'bg-blue-50/60 dark:bg-blue-900/20' : ''
                }`}
              >
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <h4 className="font-semibold text-gray-900 dark:text-gray-100">{notification.title}</h4>
                    {!notification.isRead && (
                      <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300">
                        New
                      </span>
                    )}
                  </div>
                  <p className="text-sm text-gray-600 dark:text-gray-300 mt-1">{notification.message}</p>
                  <p className="text-xs text-gray-400 dark:text-gray-500 mt-1">{new Date(notification.createdAt).toLocaleString()}</p>
                </div>
                <div className="flex items-center gap-2">
                  {!notification.isRead && (
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => markAsReadMutation.mutate(notification.id)}
                      isLoading={markAsReadMutation.isPending}
                    >
                      Mark read
                    </Button>
                  )}
                  <Button
                    size="sm"
                    variant="ghost"
                    icon={<Trash2 className="w-4 h-4" />}
                    onClick={() => deleteMutation.mutate(notification.id)}
                    isLoading={deleteMutation.isPending}
                  >
                    Delete
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>

      <Card className="p-4 space-y-4">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">Create Notification</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          <input
            type="text"
            className="border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-gray-100 dark:placeholder-gray-400 rounded-lg px-3 py-2"
            placeholder="Recipient (optional)"
            value={createForm.recipient}
            onChange={(e) => setCreateForm({ ...createForm, recipient: e.target.value })}
          />
          <input
            type="text"
            className="border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-gray-100 dark:placeholder-gray-400 rounded-lg px-3 py-2"
            placeholder="Subject (type)"
            value={createForm.subject}
            onChange={(e) => setCreateForm({ ...createForm, subject: e.target.value })}
          />
          <input
            type="text"
            className="border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-gray-100 dark:placeholder-gray-400 rounded-lg px-3 py-2"
            placeholder="Related Entity Type (templateId)"
            value={createForm.templateId}
            onChange={(e) => setCreateForm({ ...createForm, templateId: e.target.value })}
          />
          <input
            type="text"
            className="border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-gray-100 dark:placeholder-gray-400 rounded-lg px-3 py-2"
            placeholder="Related Entity Id (priority)"
            value={createForm.priority}
            onChange={(e) => setCreateForm({ ...createForm, priority: e.target.value })}
          />
        </div>
        <textarea
          className="border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-gray-100 dark:placeholder-gray-400 rounded-lg px-3 py-2 min-h-[100px]"
          placeholder="Message"
          value={createForm.message}
          onChange={(e) => setCreateForm({ ...createForm, message: e.target.value })}
        />
        <Button
          variant="primary"
          onClick={handleCreateNotification}
          isLoading={createMutation.isPending}
        >
          Create Notification
        </Button>
      </Card>

      <Card className="p-4 space-y-4">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">Create Notifications Batch</h3>
        <textarea
          className="border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-gray-100 rounded-lg px-3 py-2 min-h-[140px] font-mono text-sm"
          value={batchPayload}
          onChange={(e) => setBatchPayload(e.target.value)}
        />
        <Button
          variant="secondary"
          onClick={handleCreateBatch}
          isLoading={createBatchMutation.isPending}
        >
          Submit Batch
        </Button>
      </Card>

      <div className="flex items-center justify-between">
        <Button
          variant="secondary"
          disabled={page <= 0}
          onClick={() => setPage((prev) => Math.max(0, prev - 1))}
        >
          Previous
        </Button>
        <span className="text-sm text-gray-600 dark:text-gray-400">
          Page {page + 1} of {totalPages}
        </span>
        <Button
          variant="secondary"
          disabled={page + 1 >= totalPages}
          onClick={() => setPage((prev) => Math.min(totalPages - 1, prev + 1))}
        >
          Next
        </Button>
      </div>

      {hasAccessLevel(5) && (
        <Card className="p-4">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100 mb-3">Admin: Cleanup Old Notifications</h3>
          <div className="flex items-center gap-3">
            <input
              type="number"
              value={cleanupUserId}
              onChange={(e) => setCleanupUserId(Number(e.target.value))}
              className="border border-gray-300 dark:border-gray-600 dark:bg-gray-700 dark:text-gray-100 rounded-lg px-3 py-2 w-48"
              placeholder="User ID"
            />
            <Button
              variant="danger"
              onClick={() => cleanupMutation.mutate(cleanupUserId)}
              isLoading={cleanupMutation.isPending}
            >
              Cleanup
            </Button>
          </div>
        </Card>
      )}
    </div>
  );
};
