import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '../types';
import { STORAGE_KEYS } from '../config/constants';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  hasHydrated: boolean;
  
  setAuth: (user: User, token: string) => void;
  logout: () => void;
  updateUser: (user: User) => void;
  setHasHydrated: (hydrated: boolean) => void;
  
  // Helper methods
  hasRole: (role: string) => boolean;
  hasAccessLevel: (level: number) => boolean;
  isAdmin: () => boolean;
  isManager: () => boolean;
  isEmployee: () => boolean;
  canManageUsers: () => boolean;
  canManageClients: () => boolean;
  canManageProjects: () => boolean;
  canApproveStories: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      hasHydrated: false,

      setAuth: (user, token) => {
        console.log('[AuthStore] setAuth called with:', { user, token: token ? `${token.substring(0, 20)}...` : 'null' });
        localStorage.setItem(STORAGE_KEYS.TOKEN, token);
        localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
        console.log('[AuthStore ] Token stored in localStorage:', localStorage.getItem(STORAGE_KEYS.TOKEN) ? 'YES' : 'NO');
        set({ user, token, isAuthenticated: true });
      },

      logout: () => {
        localStorage.removeItem(STORAGE_KEYS.TOKEN);
        localStorage.removeItem(STORAGE_KEYS.USER);
        set({ user: null, token: null, isAuthenticated: false });
      },

      updateUser: (user) => {
        localStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
        set({ user });
      },

      setHasHydrated: (hydrated) => {
        set({ hasHydrated: hydrated });
      },

      hasRole: (role) => {
        const { user } = get();
        return user?.role?.toUpperCase() === role.toUpperCase();
      },

      hasAccessLevel: (level) => {
        const { user } = get();
        return (user?.accessLevel || 0) >= level;
      },

      isAdmin: () => {
        const { user } = get();
        return user?.accessLevel === 5 || user?.role?.toUpperCase() === 'ADMIN';
      },

      isManager: () => {
        const { user } = get();
        return (user?.accessLevel || 0) >= 4 || user?.role?.toUpperCase() === 'MANAGER';
      },
      
      isEmployee: () => {
        const { user } = get();
        return user?.accessLevel === 2 || user?.role?.toUpperCase() === 'EMPLOYEE';
      },
      
      canManageUsers: () => {
        const { isAdmin } = get();
        return isAdmin();
      },
      
      canManageClients: () => {
        const { isAdmin, isManager } = get();
        return isAdmin() || isManager();
      },
      
      canManageProjects: () => {
        const { isAdmin, isManager } = get();
        return isAdmin() || isManager();
      },
      
      canApproveStories: () => {
        const { isAdmin, isManager } = get();
        return isAdmin() || isManager();
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
      onRehydrateStorage: () => (state) => {
        state?.setHasHydrated(true);
      },
    }
  )
);
