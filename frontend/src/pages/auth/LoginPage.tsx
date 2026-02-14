import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { LogIn, ArrowLeft } from 'lucide-react';
import { useAuthStore } from '../../store/authStore';
import { authService } from '../../services/authService';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import toast from 'react-hot-toast';

export const LoginPage: React.FC = () => {
  // Single identifier field to accept either email or username
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { setAuth } = useAuthStore();
  const navigate = useNavigate();

  // Scroll to top on mount
  useEffect(() => {
    // Disable browser scroll restoration
    if (history.scrollRestoration !== undefined) {
      history.scrollRestoration = 'manual';
    }
    
    // Multiple attempts to scroll to top to override browser scroll restoration
    const scrollToTop = () => {
      try {
        window.scrollTo({ top: 0, left: 0, behavior: 'auto' });
        document.documentElement.scrollTop = 0;
        document.body.scrollTop = 0;
      } catch (e) {
        console.warn('Scroll error:', e);
      }
    };
    
    // Scroll immediately
    scrollToTop();
    
    // Also scroll after a short delay to override browser restoration
    const timer1 = setTimeout(scrollToTop, 0);
    const timer2 = setTimeout(scrollToTop, 50);
    const timer3 = setTimeout(scrollToTop, 100);
    
    return () => {
      clearTimeout(timer1);
      clearTimeout(timer2);
      clearTimeout(timer3);
    };
  }, []);

  const handleSubmit = async (e: React.SyntheticEvent<HTMLFormElement>) => {
    e.preventDefault();

    const normalizedIdentifier = identifier.trim();

    if (!normalizedIdentifier || !password) {
      toast.error('Please enter email/username and password');
      return;
    }

    setIsLoading(true);

    try {
      // Send both username and email fields to support either backend expectation
      const response = await authService.login({ username: normalizedIdentifier, email: normalizedIdentifier, password });
      
      console.log('[LoginPage] Login response received:', { hasToken: !!response.token, hasUser: !!response.user });
      
      if (!response.token) {
        toast.error('No authentication token received');
        setIsLoading(false);
        return;
      }

      // Use user data from login response if available, otherwise create tempUser
      const user = response.user || {
        firstName: normalizedIdentifier.split('@')[0] || 'User',
        lastName: '',
        email: normalizedIdentifier,
        accessLevel: 1,
        role: 'USER',
      };

      // Set authentication state
      setAuth(user, response.token);
      console.log('[LoginPage] Auth set, checking localStorage:', { 
        tokenExists: !!localStorage.getItem('elara_token'),
        userExists: !!localStorage.getItem('elara_user'),
        tokenLength: localStorage.getItem('elara_token')?.length
      });
      
      // Navigate to dashboard
      navigate('/dashboard');
      toast.success(`Welcome back, ${user.firstName || user.username || 'User'}!`);
    } catch (error: any) {
      console.error('[LoginPage] Login error:', error);
      console.error('[LoginPage] Error details:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      });
      
      const errorMessage = error.response?.data?.error 
        || error.response?.data?.message 
        || 'Invalid username or password';
      
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoogleLogin = () => {
    // Redirect to backend OAuth endpoint
    globalThis.location.href = '/oauth2/authorization/google';
  };

  return (
    <div className="min-h-screen w-full flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Back to Home Button */}
        <button
          onClick={() => navigate('/')}
          className="flex items-center gap-2 text-slate-600 dark:text-gray-400 hover:text-primary-600 transition-colors mb-6 group"
        >
          <ArrowLeft className="w-5 h-5 group-hover:-translate-x-1 transition-transform" />
          <span className="font-medium">Back to Home</span>
        </button>

        {/* Logo & Title */}
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gradient-to-br from-primary-500 to-secondary-500 mb-4">
            <LogIn className="w-8 h-8 text-white" />
          </div>
          <h1 className="text-3xl font-bold text-gradient mb-2">
            Elara
          </h1>
          <p className="text-slate-600 dark:text-gray-400">Elara</p>

        {/* Login Card */}
        <div className="glass-card p-8 space-y-6">
          <h2 className="text-2xl font-semibold text-slate-900 dark:text-gray-100 mb-6">Sign In</h2>
          
          {/* Email/Password Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Email or Username"
              type="text"
              value={identifier}
              onChange={(e) => setIdentifier(e.target.value)}
              placeholder="Enter your email or username"
              required
              disabled={isLoading}
            />

            <Input
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              required
              disabled={isLoading}
            />

            <Button
              type="submit"
              variant="primary"
              isLoading={isLoading}
              className="w-full"
            >
              {isLoading ? 'Signing in...' : 'Sign In'}
            </Button>
          </form>

          {/* Divider */}
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-300 dark:border-gray-600"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-2 bg-white dark:bg-gray-800 text-slate-600 dark:text-gray-400">Or continue with</span>
            </div>
          </div>

          {/* OAuth Section Header */}
          <h3 className="text-sm font-semibold text-slate-700 dark:text-gray-300">Login with OAuth 2.0</h3>

          {/* OAuth Buttons */}
          <div className="flex justify-center">
            <button
              onClick={handleGoogleLogin}
              className="flex items-center justify-center gap-2 px-6 py-2 border border-slate-300 dark:border-gray-600 rounded-lg hover:bg-slate-50 dark:hover:bg-gray-700 transition-colors font-medium text-sm w-full max-w-xs dark:text-gray-200"
            >
              <svg className="w-4 h-4" viewBox="0 0 24 24">
                <path fill="currentColor" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                <path fill="currentColor" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                <path fill="currentColor" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                <path fill="currentColor" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
              </svg>
              Google
            </button>
          </div>

          <div className="mt-6 text-center text-sm text-slate-600 dark:text-gray-400">
            <p>Demo Credentials:</p>
            <p className="font-medium">Username: admin | Password: admin123</p>
          </div>
        </div>

        {/* Footer */}
        <p className="text-center text-sm text-slate-500 dark:text-gray-500 mt-8">
          © 2026 Elara. All rights reserved.
        </p>
      </div>
    </div>
  );
};
