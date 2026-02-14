import { create } from 'zustand';

type UndoAction = {
  label: string;
  undo: () => Promise<void> | void;
  redo: () => Promise<void> | void;
};

interface UndoState {
  undoStack: UndoAction[];
  redoStack: UndoAction[];
  registerAction: (action: UndoAction) => void;
  undo: () => Promise<void> | void;
  redo: () => Promise<void> | void;
  canUndo: () => boolean;
  canRedo: () => boolean;
}

export const useUndoStore = create<UndoState>((set, get) => ({
  undoStack: [],
  redoStack: [],

  registerAction: (action) => {
    set((state) => ({
      undoStack: [...state.undoStack, action],
      redoStack: [],
    }));
  },

  undo: async () => {
    const { undoStack, redoStack } = get();
    const last = undoStack[undoStack.length - 1];
    if (!last) return;
    await last.undo();
    set({
      undoStack: undoStack.slice(0, -1),
      redoStack: [...redoStack, last],
    });
  },

  redo: async () => {
    const { undoStack, redoStack } = get();
    const last = redoStack[redoStack.length - 1];
    if (!last) return;
    await last.redo();
    set({
      undoStack: [...undoStack, last],
      redoStack: redoStack.slice(0, -1),
    });
  },

  canUndo: () => get().undoStack.length > 0,
  canRedo: () => get().redoStack.length > 0,
}));

export type { UndoAction };