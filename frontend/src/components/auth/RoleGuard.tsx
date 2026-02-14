import React from 'react';
import { useAuthStore } from '../../store/authStore';

interface RoleGuardProps {
  children: React.ReactNode;
  minLevel?: number;
  adminOnly?: boolean;
  managerOrAbove?: boolean;
  roles?: string[];
  fallback?: React.ReactNode;
}

/**
 * RoleGuard - Conditionally render content based on user role and access level
 * 
 * Usage:
 * <RoleGuard adminOnly>Admin-only content</RoleGuard>
 * <RoleGuard minLevel={4}>Manager+ content</RoleGuard>
 * <RoleGuard managerOrAbove>Manager or Admin content</RoleGuard>
 * <RoleGuard roles={['admin', 'manager']}>Specific roles</RoleGuard>
 */
export const RoleGuard: React.FC<RoleGuardProps> = ({
  children,
  minLevel,
  adminOnly = false,
  managerOrAbove = false,
  roles = [],
  fallback = null,
}) => {
  const { hasAccessLevel, isAdmin, isManager, hasRole } = useAuthStore();

  // Check admin-only access
  if (adminOnly && !isAdmin()) {
    return <>{fallback}</>;
  }

  // Check manager-or-above access
  if (managerOrAbove && !isManager() && !isAdmin()) {
    return <>{fallback}</>;
  }

  // Check minimum access level
  if (minLevel !== undefined && !hasAccessLevel(minLevel)) {
    return <>{fallback}</>;
  }

  // Check specific roles
  if (roles.length > 0 && !roles.some(role => hasRole(role))) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
};

interface ActionGuardProps {
  action: 'create' | 'edit' | 'delete' | 'approve' | 'view';
  resource: 'user' | 'client' | 'project' | 'story' | 'epic' | 'sla';
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

/**
 * ActionGuard - Control access to specific actions on resources
 * 
 * Usage:
 * <ActionGuard action="delete" resource="user">Delete button</ActionGuard>
 * <ActionGuard action="approve" resource="story">Approve button</ActionGuard>
 */
export const ActionGuard: React.FC<ActionGuardProps> = ({
  action,
  resource,
  children,
  fallback = null,
}) => {
  const { isAdmin, isManager } = useAuthStore();

  const canPerformAction = (): boolean => {
    // Admins can do everything
    if (isAdmin()) return true;

    // Define action permissions
    const permissions: Record<string, Record<string, boolean>> = {
      user: {
        create: isAdmin(),
        edit: isAdmin(),
        delete: isAdmin(),
        approve: isAdmin(),
        view: true,
      },
      client: {
        create: isManager() || isAdmin(),
        edit: isManager() || isAdmin(),
        delete: isAdmin(),
        approve: isManager() || isAdmin(),
        view: true,
      },
      project: {
        create: isManager() || isAdmin(),
        edit: isManager() || isAdmin(),
        delete: isAdmin(),
        approve: isManager() || isAdmin(),
        view: true,
      },
      story: {
        create: true,
        edit: true,
        delete: isManager() || isAdmin(),
        approve: isManager() || isAdmin(),
        view: true,
      },
      epic: {
        create: isManager() || isAdmin(),
        edit: isManager() || isAdmin(),
        delete: isManager() || isAdmin(),
        approve: isManager() || isAdmin(),
        view: true,
      },
      sla: {
        create: isManager() || isAdmin(),
        edit: isManager() || isAdmin(),
        delete: isAdmin(),
        approve: isAdmin(),
        view: isManager() || isAdmin(),
      },
    };

    return permissions[resource]?.[action] ?? false;
  };

  if (!canPerformAction()) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
};
