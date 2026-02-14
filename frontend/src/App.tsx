import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { useEffect } from 'react';
import { AppLayout } from './components/layout/AppLayout';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { LandingPage } from './pages/LandingPageEnhanced';
import { LoginPage } from './pages/auth/LoginPage';
import { OAuth2Callback } from './pages/auth/OAuth2Callback';
import { DashboardPage } from './pages/dashboard/DashboardPage';
import { ProjectsPage } from './pages/projects/ProjectsPage';
import { ProjectDetailsPage } from './pages/projects/ProjectDetailsPage';
import { StoriesPage } from './pages/stories/StoriesPage';
import { EpicsPage } from './pages/epics/EpicsPage';
import { UsersPage } from './pages/users/UsersPage';
import { UserDetailsPage } from './pages/users/UserDetailsPage';
import { ClientsPage } from './pages/clients/ClientsPage';
import { SlaRulesPage } from './pages/sla/SlaRulesPage';
import { ReportsPage } from './pages/reports/ReportsPage';
import { ProfilePage } from './pages/profile/ProfilePage';
import { NotificationsPage } from './pages/notifications/NotificationsPage';
import { EmailPage } from './pages/tools/EmailPage';
import { FilesPage } from './pages/tools/FilesPage';
import { AuditLogsPage } from './pages/admin/AuditLogsPage';
import { useAuthStore } from './store/authStore';
import { useUndoRedoStore } from './store/undoRedoStore';
import { useThemeStore } from './store/themeStore';

// Create a client
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 5 * 60 * 1000, // 5 minutes
    },
  },
});

function App() {
  const { isAuthenticated, hasHydrated, logout, token } = useAuthStore();
  const { undo, redo, canUndo, canRedo } = useUndoRedoStore();
  const theme = useThemeStore((s) => s.theme);

  // Apply theme class on mount and when it changes
  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [theme]);

  // Validate token ONLY once on initial page load
  useEffect(() => {
    if (!hasHydrated) return;
    if (!isAuthenticated) return;
    
    // Check the Zustand-persisted token (from 'auth-storage' in localStorage)
    const storedToken = token;
    if (!storedToken) return;
    
    // Check if JWT is expired
    try {
      const parts = storedToken.split('.');
      if (parts.length !== 3) throw new Error('Not a JWT');
      const payload = JSON.parse(atob(parts[1]));
      if (payload.exp && Date.now() >= payload.exp * 1000) {
        console.warn('[App] Stored token is expired, logging out');
        logout();
      }
    } catch {
      // Not a valid JWT - clear it
      console.warn('[App] Invalid stored token, logging out');
      logout();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [hasHydrated]); // Only run when hydration completes, NOT on every auth change

  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      const target = event.target as HTMLElement | null;
      const isInput = target && ['INPUT', 'TEXTAREA'].includes(target.tagName);
      if (isInput || target?.isContentEditable) return;

      const isMac = navigator.platform.toUpperCase().includes('MAC');
      const cmdOrCtrl = isMac ? event.metaKey : event.ctrlKey;

      if (cmdOrCtrl && event.key.toLowerCase() === 'z') {
        event.preventDefault();
        if (event.shiftKey) {
          // Cmd/Ctrl+Shift+Z = Redo
          if (canRedo()) redo();
        } else {
          // Cmd/Ctrl+Z = Undo
          if (canUndo()) undo();
        }
      } else if (cmdOrCtrl && event.key.toLowerCase() === 'y') {
        event.preventDefault();
        // Cmd/Ctrl+Y = Redo
        if (canRedo()) redo();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [undo, redo, canUndo, canRedo]);

  if (!hasHydrated) {
    return (
      <div className="flex items-center justify-center h-screen bg-slate-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4"></div>
          <p className="text-slate-600">Setting things up...</p>
        </div>
      </div>
    );
  }

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={isAuthenticated ? <Navigate to="/dashboard" replace /> : <LandingPage />} />
          <Route
            path="/login"
            element={
              isAuthenticated ? <Navigate to="/dashboard" replace /> : <LoginPage />
            }
          />
          <Route path="/oauth2/callback" element={<OAuth2Callback />} />

          {/* Protected Routes */}
          <Route
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route
              path="/projects"
              element={
                <ProtectedRoute requiredLevel={1}>
                  <ProjectsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/projects/:id"
              element={
                <ProtectedRoute requiredLevel={1}>
                  <ProjectDetailsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/epics"
              element={
                <ProtectedRoute requiredLevel={2}>
                  <EpicsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/stories"
              element={
                <ProtectedRoute requiredLevel={2}>
                  <StoriesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/users"
              element={
                <ProtectedRoute requiredLevel={1}>
                  <UsersPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/users/:id"
              element={
                <ProtectedRoute requiredLevel={1}>
                  <UserDetailsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/clients"
              element={
                <ProtectedRoute requiredLevel={1}>
                  <ClientsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/sla-rules"
              element={
                <ProtectedRoute requiredLevel={3}>
                  <SlaRulesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/reports"
              element={
                <ProtectedRoute requiredLevel={2}>
                  <ReportsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/notifications"
              element={
                <ProtectedRoute requiredLevel={1}>
                  <NotificationsPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/email"
              element={
                <ProtectedRoute requiredLevel={1}>
                  <EmailPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/files"
              element={
                <ProtectedRoute requiredLevel={1}>
                  <FilesPage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile"
              element={
                <ProtectedRoute requiredLevel={1}>
                  <ProfilePage />
                </ProtectedRoute>
              }
            />
            <Route
              path="/admin/audit-logs"
              element={
                <ProtectedRoute requiredLevel={5}>
                  <AuditLogsPage />
                </ProtectedRoute>
              }
            />
          </Route>

          {/* Default redirect */}
          <Route
            path="/"
            element={<Navigate to={isAuthenticated ? '/dashboard' : '/login'} replace />}
          />
          
          {/* 404 */}
          <Route path="*" element={<div className="flex items-center justify-center h-screen"><h1 className="text-2xl font-bold">404 - Page Not Found</h1></div>} />
        </Routes>
      </BrowserRouter>

      {/* Toast Notifications */}
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 3000,
          style: {
            background: '#fff',
            color: '#1e293b',
            padding: '16px',
            borderRadius: '12px',
            boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)',
          },
          success: {
            iconTheme: {
              primary: '#10b981',
              secondary: '#fff',
            },
          },
          error: {
            iconTheme: {
              primary: '#ef4444',
              secondary: '#fff',
            },
          },
        }}
      />
    </QueryClientProvider>
  );
}

export default App;
