import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  FolderKanban,
  GitBranch,
  CheckSquare,
  Users,
  Building2,
  Bell,
  BellRing,
  FileText,
  Settings,
  LogOut,
  ChevronLeft,
  Mail,
  FolderUp,
  Activity,
} from 'lucide-react';
import { useAuthStore } from '../../store/authStore';
import { useUIStore } from '../../store/uiStore';

export const Sidebar: React.FC = () => {
  const { user, logout, isAdmin, isManager, hasAccessLevel } = useAuthStore();
  const { sidebarOpen, toggleSidebar } = useUIStore();

  // Define navigation with granular access control
  // Access levels: 1=User, 2=Employee, 3=Lead, 4=Manager, 5=Admin
  const navigation = [
    { name: 'Dashboard', icon: LayoutDashboard, path: '/dashboard', minLevel: 1 },
    { name: 'Projects', icon: FolderKanban, path: '/projects', minLevel: 1 },
    { name: 'Epics', icon: GitBranch, path: '/epics', minLevel: 2 },
    { name: 'Stories', icon: CheckSquare, path: '/stories', minLevel: 1 },
    { name: 'Team', icon: Users, path: '/users', minLevel: 1 }, // All users can view team
    { name: 'Clients', icon: Building2, path: '/clients', minLevel: 4, managerOrAbove: true }, // Manager+
    { name: 'SLA Rules', icon: Bell, path: '/sla-rules', minLevel: 4, managerOrAbove: true }, // Manager+
    { name: 'Reports', icon: FileText, path: '/reports', minLevel: 3 },
    { name: 'Notifications', icon: BellRing, path: '/notifications', minLevel: 1 },
    { name: 'Email', icon: Mail, path: '/email', minLevel: 1 },
    { name: 'Files', icon: FolderUp, path: '/files', minLevel: 1 },
    { name: 'Audit Logs', icon: Activity, path: '/admin/audit-logs', minLevel: 5, adminOnly: true }, // Admin only
    { name: 'Profile', icon: Settings, path: '/profile', minLevel: 1 },
  ];

  // Filter navigation based on user role and access level
  const filteredNavigation = navigation.filter(item => {
    // Check base access level
    if (!hasAccessLevel(item.minLevel)) return false;
    
    // Check admin-only items
    if (item.adminOnly && !isAdmin()) return false;
    
    // Check manager-or-above items
    if (item.managerOrAbove && !isManager() && !isAdmin()) return false;
    
    return true;
  });

  if (!sidebarOpen) {
    return (
      <div className="fixed left-0 top-0 h-full w-16 glass-card z-40 flex flex-col items-center py-4">
        <button
          onClick={toggleSidebar}
          className="p-2 hover:bg-primary-50 dark:hover:bg-gray-700 rounded-lg transition-colors mb-8"
        >
          <ChevronLeft className="w-6 h-6 text-primary-600 rotate-180" />
        </button>
        {filteredNavigation.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              `p-3 mb-2 rounded-lg transition-colors ${
                isActive
                  ? 'bg-gradient-to-r from-primary-500 to-primary-600 text-white'
                  : 'text-slate-600 dark:text-gray-300 hover:bg-primary-50 dark:hover:bg-gray-700'
              }`
            }
            title={item.name}
          >
            <item.icon className="w-5 h-5" />
          </NavLink>
        ))}
      </div>
    );
  }

  return (
    <aside className="fixed left-0 top-0 h-full w-64 glass-card z-40 flex flex-col">
      {/* Logo & Header */}
      <div className="px-6 py-6 border-b border-white/20">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold text-gradient">Elara</h1>
            <p className="text-xs text-slate-500 dark:text-gray-400 mt-1">Elara</p>
          </div>
          <button
            onClick={toggleSidebar}
            className="p-2 hover:bg-primary-50 dark:hover:bg-gray-700 rounded-lg transition-colors"
          >
            <ChevronLeft className="w-5 h-5 text-primary-600" />
          </button>
        </div>
      </div>

      {/* User Info */}
      <div className="px-6 py-4 border-b border-white/20">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary-500 to-secondary-500 flex items-center justify-center text-white font-semibold">
            {user?.firstName?.charAt(0).toUpperCase()}{user?.lastName?.charAt(0).toUpperCase()}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-slate-900 dark:text-gray-100 truncate">{user?.firstName} {user?.lastName}</p>
            <p className="text-xs text-slate-500 dark:text-gray-400 truncate">{user?.role}</p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-4 py-4 overflow-y-auto scrollbar-hide">
        <div className="space-y-1">
          {filteredNavigation.map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                isActive ? 'sidebar-link sidebar-link-active' : 'sidebar-link'
              }
            >
              <item.icon className="w-5 h-5" />
              <span>{item.name}</span>
            </NavLink>
          ))}
        </div>
      </nav>

      {/* Logout */}
      <div className="px-4 py-4 border-t border-white/20">
        <button
          onClick={logout}
          className="sidebar-link w-full text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20"
        >
          <LogOut className="w-5 h-5" />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  );
};
