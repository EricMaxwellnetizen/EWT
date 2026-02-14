import { useCallback } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useUndoRedoStore } from '../store/undoRedoStore';
import { activityService } from '../services/activityService';
import toast from 'react-hot-toast';
import { X } from 'lucide-react';

interface UndoRedoAction {
  id: string;
  type: 'create' | 'update' | 'delete';
  entityType: 'project' | 'epic' | 'story' | 'client' | 'sla' | 'user';
  entityId: string | number;
  entityName?: string;
  timestamp: number;
  previousState?: Record<string, any>;
  newState?: Record<string, any>;
  description: string;
  undoFn: () => Promise<void>;
  redoFn: () => Promise<void>;
}

interface TrackMutationOptions {
  type?: 'create' | 'update' | 'delete';
  description: string;
  entityType: 'project' | 'epic' | 'story' | 'client' | 'sla' | 'user';
  entityId: string | number;
  entityName?: string;
  previousState?: Record<string, any>;
  newState?: Record<string, any>;
  onUndo: () => Promise<void>;
  onRedo: () => Promise<void>;
  queryKeyToInvalidate?: string[];
}

export const useUndoRedo = () => {
  const { addAction, undo, redo, canUndo, canRedo, getLastAction } = useUndoRedoStore();
  const queryClient = useQueryClient();

  const trackMutation = useCallback(
    async (options: TrackMutationOptions) => {
      // Determine action type from description if not provided
      let actionType = options.type || 'update' as 'create' | 'update' | 'delete';
      if (options.description.toLowerCase().includes('deleted')) {
        actionType = 'delete';
      } else if (options.description.toLowerCase().includes('created')) {
        actionType = 'create';
      }

      const action: UndoRedoAction = {
        id: `${options.entityType}-${options.entityId}-${Date.now()}`,
        type: actionType,
        entityType: options.entityType,
        entityId: options.entityId,
        entityName: options.entityName,
        timestamp: Date.now(),
        previousState: options.previousState,
        newState: options.newState,
        description: options.description,
        undoFn: async () => {
          try {
            await options.onUndo();
            if (options.queryKeyToInvalidate) {
              options.queryKeyToInvalidate.forEach((key) => {
                queryClient.invalidateQueries({ queryKey: [key] });
              });
            }
          } catch (error) {
            console.error('Undo failed:', error);
            throw error;
          }
        },
        redoFn: async () => {
          try {
            await options.onRedo();
            if (options.queryKeyToInvalidate) {
              options.queryKeyToInvalidate.forEach((key) => {
                queryClient.invalidateQueries({ queryKey: [key] });
              });
            }
          } catch (error) {
            console.error('Redo failed:', error);
            throw error;
          }
        },
      };

      addAction(action);
      
      // Log activity to notification system
      await activityService.logActivity({
        type: actionType,
        entityType: options.entityType,
        entityId: options.entityId,
        entityName: options.entityName || options.description,
        description: options.description,
        timestamp: Date.now(),
      }).catch(console.error);
      
      // Show the undo/redo toast
      showUndoRedoToast(action);
    },
    [addAction, queryClient]
  );

  const handleUndo = useCallback(async () => {
    if (!canUndo()) return;
    try {
      await undo();
      toast.success('Undone (Ctrl+Y to redo)', { duration: 2000 });
    } catch (error) {
      toast.error('Failed to undo', { duration: 2000 });
    }
  }, [undo, canUndo]);

  const handleRedo = useCallback(async () => {
    if (!canRedo()) return;
    try {
      await redo();
      toast.success('Redone (Ctrl+Z to undo)', { duration: 2000 });
    } catch (error) {
      toast.error('Failed to redo', { duration: 2000 });
    }
  }, [redo, canRedo]);

  return {
    trackMutation,
    undo: handleUndo,
    redo: handleRedo,
    canUndo: canUndo(),
    canRedo: canRedo(),
    getLastAction,
  };
};

const showUndoRedoToast = (action: UndoRedoAction) => {
  toast.custom(
    (t) => (
      <div className="flex items-center gap-3 bg-white border border-slate-200 rounded-lg px-4 py-3 shadow-lg backdrop-blur-sm">
        <div className="flex-1">
          <p className="text-sm font-medium text-slate-900">{action.description}</p>
          <p className="text-xs text-slate-500 mt-0.5">Press Ctrl+Z to undo anytime</p>
        </div>
        <button
          onClick={() => {
            const { undo } = useUndoRedoStore.getState();
            undo().catch(() => toast.error('Undo failed'));
            toast.dismiss(t.id);
          }}
          className="flex-shrink-0 px-3 py-1.5 text-sm font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 rounded transition-colors"
          title="Undo this action"
        >
          Undo
        </button>
        <button
          onClick={() => toast.dismiss(t.id)}
          className="flex-shrink-0 p-1 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded transition-colors"
          title="Dismiss"
        >
          <X className="w-4 h-4" />
        </button>
      </div>
    ),
    {
      duration: 10000,
      position: 'top-right',
    }
  );
};
