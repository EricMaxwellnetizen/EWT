import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, Mail, Phone, Calendar, Briefcase, User as UserIcon, Shield } from 'lucide-react';
import { userService } from '../../services/userService';
import { useAuthStore } from '../../store/authStore';
import { Card, CardBody, CardHeader } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Loader } from '../../components/ui/Loader';
import { Badge } from '../../components/ui/Badge';
import { formatDate } from '../../utils/helpers';

export const UserDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { hasHydrated } = useAuthStore();

  const { data: user, isLoading, error } = useQuery({
    queryKey: ['user', id],
    queryFn: () => userService.getById(Number(id)),
    enabled: !!id && hasHydrated,
  });

  if (isLoading) {
    return <Loader />;
  }

  if (error || !user) {
    return (
      <div className="p-6">
        <Card>
          <CardBody>
            <div className="text-center py-12">
              <p className="text-slate-600 mb-4">User not found</p>
              <Button onClick={() => navigate('/users')}>
                Back to Team
              </Button>
            </div>
          </CardBody>
        </Card>
      </div>
    );
  }

  const getAccessLevelLabel = (level: number): string => {
    switch (level) {
      case 1: return 'User';
      case 2: return 'Employee';
      case 3: return 'Manager';
      case 4: return 'Senior Manager';
      case 5: return 'Admin';
      case 6: return 'Super Admin';
      default: return 'Unknown';
    }
  };

  const getAccessLevelColor = (level: number): 'info' | 'success' | 'warning' | 'danger' => {
    if (level >= 5) return 'danger';
    if (level >= 3) return 'warning';
    if (level >= 2) return 'success';
    return 'info';
  };

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button
            variant="secondary"
            icon={<ArrowLeft className="w-4 h-4" />}
            onClick={() => navigate('/users')}
          >
            Back
          </Button>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">User Details</h1>
            <p className="text-gray-600 mt-1">View user information</p>
          </div>
        </div>
      </div>

      {/* User Profile Card */}
      <Card glass>
        <CardHeader>
          <div className="flex items-center gap-4">
            <div className="w-20 h-20 rounded-full bg-gradient-to-br from-primary-500 to-secondary-500 flex items-center justify-center text-white font-bold text-3xl">
              {(user.firstName?.charAt(0) || '') + (user.lastName?.charAt(0) || '')}
            </div>
            <div className="flex-1">
              <h2 className="text-2xl font-bold text-gray-900">
                {user.firstName} {user.lastName}
              </h2>
              <p className="text-gray-600">@{user.username}</p>
            </div>
            <Badge variant={getAccessLevelColor(user.accessLevel)}>
              {getAccessLevelLabel(user.accessLevel)}
            </Badge>
          </div>
        </CardHeader>
        <CardBody>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Contact Information */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-gray-900 mb-3">Contact Information</h3>
              
              <div className="flex items-start gap-3">
                <Mail className="w-5 h-5 text-gray-400 mt-0.5" />
                <div>
                  <p className="text-xs text-gray-500">Email</p>
                  <p className="text-sm font-medium text-gray-900">{user.email}</p>
                </div>
              </div>

              {user.phoneNumber && (
                <div className="flex items-start gap-3">
                  <Phone className="w-5 h-5 text-gray-400 mt-0.5" />
                  <div>
                    <p className="text-xs text-gray-500">Phone</p>
                    <p className="text-sm font-medium text-gray-900">{user.phoneNumber}</p>
                  </div>
                </div>
              )}
            </div>

            {/* Professional Information */}
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-gray-900 mb-3">Professional Information</h3>
              
              {user.department && (
                <div className="flex items-start gap-3">
                  <Briefcase className="w-5 h-5 text-gray-400 mt-0.5" />
                  <div>
                    <p className="text-xs text-gray-500">Department</p>
                    <p className="text-sm font-medium text-gray-900">{user.department}</p>
                  </div>
                </div>
              )}

              {user.jobTitle && (
                <div className="flex items-start gap-3">
                  <UserIcon className="w-5 h-5 text-gray-400 mt-0.5" />
                  <div>
                    <p className="text-xs text-gray-500">Job Title</p>
                    <p className="text-sm font-medium text-gray-900">{user.jobTitle}</p>
                  </div>
                </div>
              )}

              <div className="flex items-start gap-3">
                <Shield className="w-5 h-5 text-gray-400 mt-0.5" />
                <div>
                  <p className="text-xs text-gray-500">Role</p>
                  <p className="text-sm font-medium text-gray-900">{user.role}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Additional Information */}
          <div className="mt-6 pt-6 border-t border-gray-200">
            <h3 className="text-lg font-semibold text-gray-900 mb-3">Additional Information</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {user.joiningDate && (
                <div className="flex items-start gap-3">
                  <Calendar className="w-5 h-5 text-gray-400 mt-0.5" />
                  <div>
                    <p className="text-xs text-gray-500">Joining Date</p>
                    <p className="text-sm font-medium text-gray-900">{formatDate(user.joiningDate)}</p>
                  </div>
                </div>
              )}

              {user.createdAt && (
                <div className="flex items-start gap-3">
                  <Calendar className="w-5 h-5 text-gray-400 mt-0.5" />
                  <div>
                    <p className="text-xs text-gray-500">Account Created</p>
                    <p className="text-sm font-medium text-gray-900">{formatDate(user.createdAt)}</p>
                  </div>
                </div>
              )}

              {user.reportingToUsername && (
                <div className="flex items-start gap-3">
                  <UserIcon className="w-5 h-5 text-gray-400 mt-0.5" />
                  <div>
                    <p className="text-xs text-gray-500">Reports To</p>
                    <p className="text-sm font-medium text-gray-900">{user.reportingToUsername}</p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </CardBody>
      </Card>
    </div>
  );
};
