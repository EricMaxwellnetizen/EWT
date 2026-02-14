import { useCallback } from 'react';
import toast from 'react-hot-toast';

type UndoToastOptions = {
  message: string;
  onUndo: () => void;
  onRedo: () => void;
};

export const showUndoToast = ({ message, onUndo, onRedo }: UndoToastOptions) => {
  const toastId = toast.custom((t) => (
    <div
      className={`flex items-center gap-3 rounded-lg bg-white px-4 py-3 shadow-lg border border-slate-200 ${
        t.visible ? 'animate-enter' : 'animate-leave'
      }`}
    >
      <span className="text-sm text-slate-800">{message}</span>
      <div className="ml-auto flex items-center gap-2">
        <button
          className="text-sm font-medium text-primary-600 hover:text-primary-700"
          onClick={() => {
            onUndo();
            toast.dismiss(toastId);
          }}
        >
          Undo
        </button>
        <button
          className="text-sm font-medium text-slate-600 hover:text-slate-800"
          onClick={() => {
            onRedo();
            toast.dismiss(toastId);
          }}
        >
          Redo
        </button>
      </div>
    </div>
  ));

  setTimeout(() => {
    toast.dismiss(toastId);
  }, 10000);

  return toastId;
};