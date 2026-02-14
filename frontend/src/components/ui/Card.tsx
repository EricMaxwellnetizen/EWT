import React from 'react';

interface CardProps {
  children: React.ReactNode;
  className?: string;
  hover?: boolean;
  glass?: boolean;
  onClick?: () => void;
}

export const Card: React.FC<CardProps> = ({
  children,
  className = '',
  hover = false,
  glass = false,
  onClick,
}) => {
  const baseStyles = 'rounded-2xl overflow-hidden';
  const glassStyles = glass ? 'glass-card' : 'bg-white dark:bg-gray-800 shadow-lg border border-slate-200 dark:border-gray-700';
  const hoverStyles = hover ? 'card-hover cursor-pointer' : '';
  
  return (
    <div
      className={`${baseStyles} ${glassStyles} ${hoverStyles} ${className}`}
      onClick={onClick}
    >
      {children}
    </div>
  );
};

export const CardHeader: React.FC<{ children: React.ReactNode; className?: string }> = ({
  children,
  className = '',
}) => {
  return <div className={`px-6 py-4 border-b border-slate-200 dark:border-gray-700 ${className}`}>{children}</div>;
};

export const CardBody: React.FC<{ children: React.ReactNode; className?: string }> = ({
  children,
  className = '',
}) => {
  return <div className={`px-6 py-4 ${className}`}>{children}</div>;
};

export const CardFooter: React.FC<{ children: React.ReactNode; className?: string }> = ({
  children,
  className = '',
}) => {
  return <div className={`px-6 py-4 border-t border-slate-200 dark:border-gray-700 ${className}`}>{children}</div>;
};
