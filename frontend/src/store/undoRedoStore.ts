import { create } from 'zustand';

interface UndoRedoAction {
  id: string;
  type: 'create' | 'update' | 'delete';
  entityType: 'project' | 'epic' | 'story' | 'client' | 'sla' | 'user';
  entityId: string | number;
  timestamp: number;
  previousState?: Record<string, any>;
  newState?: Record<string, any>;
  description: string;
  undoFn: () => Promise<void>;
  redoFn: () => Promise<void>;
}

interface UndoRedoState {
  history: UndoRedoAction[];
  futureStack: UndoRedoAction[];
  isUndoing: boolean;
  isRedoing: boolean;
  lastActionTime: number;
  
  // Methods
  addAction: (action: UndoRedoAction) => void;
  undo: () => Promise<void>;
  redo: () => Promise<void>;
  canUndo: () => boolean;
  canRedo: () => boolean;
  clear: () => void;
  getLastAction: () => UndoRedoAction | null;
}

const MAX_HISTORY = 50; // Store last 50 actions

export const useUndoRedoStore = create<UndoRedoState>((set, get) => ({
  history: [],
  futureStack: [],
  isUndoing: false,
  isRedoing: false,
  lastActionTime: 0,

  addAction: (action: UndoRedoAction) => {
    set((state) => {
      // When a new action is performed, clear the future stack
      const newHistory = [...state.history, action];
      
      // Keep only the last MAX_HISTORY actions to prevent memory bloat
      if (newHistory.length > MAX_HISTORY) {
        newHistory.shift();
      }

      return {
        history: newHistory,
        futureStack: [], // Clear redo stack when new action is performed
        lastActionTime: Date.now(),
      };
    });
  },

  undo: async () => {
    const state = get();
    if (!state.canUndo()) return;

    const action = state.history[state.history.length - 1];
    
    try {
      set({ isUndoing: true });
      await action.undoFn();
      
      set((s) => ({
        history: s.history.slice(0, -1),
        futureStack: [action, ...s.futureStack],
        lastActionTime: Date.now(),
      }));
    } finally {
      set({ isUndoing: false });
    }
  },

  redo: async () => {
    const state = get();
    if (!state.canRedo()) return;

    const action = state.futureStack[0];
    
    try {
      set({ isRedoing: true });
      await action.redoFn();
      
      set((s) => ({
        history: [...s.history, action],
        futureStack: s.futureStack.slice(1),
        lastActionTime: Date.now(),
      }));
    } finally {
      set({ isRedoing: false });
    }
  },

  canUndo: () => {
    const state = get();
    return state.history.length > 0 && !state.isUndoing && !state.isRedoing;
  },

  canRedo: () => {
    const state = get();
    return state.futureStack.length > 0 && !state.isUndoing && !state.isRedoing;
  },

  clear: () => {
    set({ history: [], futureStack: [], lastActionTime: 0 });
  },

  getLastAction: () => {
    const state = get();
    return state.history.length > 0 ? state.history[state.history.length - 1] : null;
  },
}));
