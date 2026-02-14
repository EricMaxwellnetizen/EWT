import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { authService } from '../../services/authService';
import { Loader } from '../../components/ui/Loader';
import toast from 'react-hot-toast';

export const OAuth2Callback: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { setAuth } = useAuthStore();

  useEffect(() => {
    const handleCallback = async () => {
      try {
        const token = searchParams.get('token');
        const userId = searchParams.get('userId');

        if (!token) {
          toast.error('OAuth login failed: No token received');
          navigate('/login');
          return;
        }

        // Fetch user details using the token
        let user;
        try {
          // Temporarily set token in store for API requests
          const tempUser: any = {
            id: 0,
            firstName: 'User',
            lastName: '',
            email: '',
            username: '',
            accessLevel: 1,
            role: 'USER',
          };
          setAuth(tempUser, token);
          
          // Now fetch actual user details
          user = await authService.getCurrentUser();
        } catch (userFetchError) {
          console.warn('Failed to fetch user details:', userFetchError);
          // Fallback: create user object from token
          user = {
            id: Number(userId) || 0,
            firstName: 'User',
            lastName: '',
            email: '',
            username: '',
            accessLevel: 1,
            role: 'USER',
          };
        }

        // Set final authentication state with complete user data
        setAuth(user, token);

        toast.success('OAuth login successful!');
        navigate('/dashboard');
      } catch (error) {
        console.error('OAuth callback error:', error);
        toast.error('OAuth login failed. Please try again.');
        navigate('/login');
      }
    };

    handleCallback();
  }, [searchParams, navigate, setAuth]);

  return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="text-center">
        <Loader />
        <p className="mt-4 text-slate-600">Completing your sign-in...</p>
      </div>
    </div>
  );
};
