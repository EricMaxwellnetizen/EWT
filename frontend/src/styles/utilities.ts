/**
 * Reusable CSS utility classes
 * These classes can be used across components for consistency
 */

export const buttonStyles = {
  base: 'inline-flex items-center justify-center rounded-lg font-medium transition-all focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed',
  variants: {
    primary: 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500',
    secondary: 'bg-gray-200 text-gray-900 hover:bg-gray-300 focus:ring-gray-500',
    success: 'bg-green-600 text-white hover:bg-green-700 focus:ring-green-500',
    danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
    outline: 'border-2 border-gray-300 bg-transparent hover:bg-gray-50 focus:ring-gray-500',
  },
  sizes: {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-base',
    lg: 'px-6 py-3 text-lg',
  },
};

export const cardStyles = {
  base: 'rounded-lg border border-slate-200 bg-white shadow-sm',
  hover: 'hover:shadow-md transition-shadow',
  interactive: 'cursor-pointer hover:border-blue-300 hover:shadow-lg',
};

export const inputStyles = {
  base: 'w-full rounded-lg border border-gray-300 px-4 py-2 text-gray-900 placeholder-gray-400 focus:border-blue-500 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100 disabled:cursor-not-allowed',
  error: 'border-red-500 focus:border-red-500 focus:ring-red-500',
  success: 'border-green-500 focus:border-green-500 focus:ring-green-500',
};

export const badgeStyles = {
  base: 'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold',
  variants: {
    default: 'bg-gray-100 text-gray-800',
    primary: 'bg-blue-100 text-blue-800',
    success: 'bg-green-100 text-green-800',
    warning: 'bg-yellow-100 text-yellow-800',
    danger: 'bg-red-100 text-red-800',
    info: 'bg-cyan-100 text-cyan-800',
  },
};

export const modalStyles = {
  overlay: 'fixed inset-0 bg-black/50 backdrop-blur-sm z-50',
  container: 'fixed inset-0 z-50 flex items-center justify-center p-4',
  content: 'relative max-h-[90vh] w-full max-w-lg overflow-auto rounded-lg bg-white shadow-xl',
  header: 'sticky top-0 z-10 border-b border-gray-200 bg-white px-6 py-4',
  body: 'px-6 py-4',
  footer: 'sticky bottom-0 z-10 border-t border-gray-200 bg-gray-50 px-6 py-4',
};

export const tableStyles = {
  container: 'overflow-x-auto rounded-lg border border-gray-200',
  table: 'min-w-full divide-y divide-gray-200',
  thead: 'bg-gray-50',
  th: 'px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500',
  tbody: 'divide-y divide-gray-200 bg-white',
  td: 'whitespace-nowrap px-6 py-4 text-sm text-gray-900',
  row: 'hover:bg-gray-50 transition-colors',
};

export const loadingStyles = {
  spinner: 'animate-spin rounded-full border-4 border-gray-200 border-t-blue-600',
  overlay: 'fixed inset-0 z-50 flex items-center justify-center bg-white/80',
  text: 'mt-4 text-gray-600',
};

export const alertStyles = {
  base: 'rounded-lg p-4 flex items-start gap-3',
  variants: {
    info: 'bg-blue-50 text-blue-800 border border-blue-200',
    success: 'bg-green-50 text-green-800 border border-green-200',
    warning: 'bg-yellow-50 text-yellow-800 border border-yellow-200',
    error: 'bg-red-50 text-red-800 border border-red-200',
  },
};
