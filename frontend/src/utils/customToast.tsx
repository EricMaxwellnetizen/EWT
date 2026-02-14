import React from 'react';
import toast, { type Toast } from 'react-hot-toast';
import { X } from 'lucide-react';

interface CustomToastOptions {
  duration?: number;
  type?: 'success' | 'error' | 'info' | 'custom';
}

export const showCustomToast = (
  message: string | React.ReactNode,
  options?: CustomToastOptions
) => {
  const { duration = 4000, type = 'info' } = options || {};

  return toast.custom(
    (t) => (
      <div
        className={`flex items-center gap-3 px-4 py-3 rounded-lg shadow-lg backdrop-blur-sm border ${
          type === 'success'
            ? 'bg-green-50/95 border-green-200 text-green-900'
            : type === 'error'
            ? 'bg-red-50/95 border-red-200 text-red-900'
            : 'bg-blue-50/95 border-blue-200 text-blue-900'
        }`}
      >
        <div className="flex-1">
          {typeof message === 'string' ? (
            <p className="text-sm font-medium">{message}</p>
          ) : (
            message
          )}
        </div>
        <button
          onClick={() => toast.dismiss(t.id)}
          className={`flex-shrink-0 p-1 rounded hover:bg-white/50 transition-colors ${
            type === 'success'
              ? 'text-green-600'
              : type === 'error'
              ? 'text-red-600'
              : 'text-blue-600'
          }`}
          title="Dismiss"
        >
          <X className="w-4 h-4" />
        </button>
      </div>
    ),
    {
      duration,
      position: 'top-right',
    }
  );
};

export const showSuccessToast = (message: string, duration?: number) =>
  showCustomToast(message, { type: 'success', duration });

export const showErrorToast = (message: string, duration?: number) =>
  showCustomToast(message, { type: 'error', duration });

export const showInfoToast = (message: string, duration?: number) =>
  showCustomToast(message, { type: 'info', duration });
