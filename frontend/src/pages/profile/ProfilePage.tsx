import React, { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { User as UserIcon, Mail, Phone, Building2, Shield, Lock, Save, Upload } from 'lucide-react';
import { userService } from '../../services/userService';
import { timeLogService, type TimeLog } from '../../services/timeLogService';
import type { UserInput } from '../../types';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { Badge } from '../../components/ui/Badge';
import { useAuthStore } from '../../store/authStore';
import toast from 'react-hot-toast';
import { formatDate } from '../../utils/helpers';

export const ProfilePage: React.FC = () => {
  const queryClient = useQueryClient();
  const { user, setAuth } = useAuthStore();
  const [isEditingProfile, setIsEditingProfile] = useState(false);
  const [isChangingPassword, setIsChangingPassword] = useState(false);
  const [previewUrl, setPreviewUrl] = useState<string>('');
  const [timeLogStart, setTimeLogStart] = useState('');
  const [timeLogEnd, setTimeLogEnd] = useState('');
  const [timeLogs, setTimeLogs] = useState<TimeLog[]>([]);
  const [timeLogTotal, setTimeLogTotal] = useState<number | null>(null);
  const [timeLogLoading, setTimeLogLoading] = useState(false);

  // Derive display name from firstName/lastName or fall back to username
  const displayFirst = user?.firstName || user?.username || '';
  const displayLast = user?.lastName || '';
  const initials = (displayFirst[0] || '') + (displayLast[0] || displayFirst[1] || '');

  const [profileData, setProfileData] = useState<Partial<UserInput>>({
    firstName: user?.firstName || user?.username || '',
    lastName: user?.lastName || '',
    email: user?.email || '',
    phoneNumber: user?.phoneNumber || '',
    department: user?.department || '',
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  // Update profile mutation
  const updateProfileMutation = useMutation({
    mutationFn: async (data: Partial<UserInput>) => {
      return userService.updateProfile(data);
    },
    onSuccess: (updatedUser) => {
      // Update the auth store with the new user data
      setAuth(updatedUser, user?.id?.toString() || '');
      queryClient.invalidateQueries({ queryKey: ['currentUser'] });
      toast.success('Profile updated successfully');
      setIsEditingProfile(false);
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to update profile');
    },
  });

  // Change password mutation
  const changePasswordMutation = useMutation({
    mutationFn: async (data: { oldPassword: string; newPassword: string }) => {
      return userService.changePassword(data.oldPassword, data.newPassword);
    },
    onSuccess: () => {
      toast.success('Password changed successfully');
      setIsChangingPassword(false);
      setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to change password');
    },
  });

  // Upload profile picture mutation
  const uploadProfilePicMutation = useMutation({
    mutationFn: async (file: File) => {
      return userService.uploadProfilePicture(file);
    },
    onSuccess: () => {
      toast.success('Profile picture uploaded successfully');
      // Invalidate queries to refresh user data
      queryClient.invalidateQueries({ queryKey: ['currentUser'] });
      setPreviewUrl('');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to upload profile picture');
    },
  });

  const handleProfileSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!profileData.firstName || !profileData.lastName || !profileData.email) {
      toast.error('Please fill in all required fields');
      return;
    }
    updateProfileMutation.mutate(profileData);
  };

  const handlePasswordSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!passwordData.currentPassword || !passwordData.newPassword || !passwordData.confirmPassword) {
      toast.error('Please fill in all password fields');
      return;
    }
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast.error('New passwords do not match');
      return;
    }
    if (passwordData.newPassword.length < 6) {
      toast.error('Password must be at least 6 characters long');
      return;
    }
    changePasswordMutation.mutate({
      oldPassword: passwordData.currentPassword,
      newPassword: passwordData.newPassword,
    });
  };

  const handleLoadTimeLogs = async () => {
    if (!user?.id) return;
    if (!timeLogStart || !timeLogEnd) {
      toast.error('Please select a start and end date');
      return;
    }
    setTimeLogLoading(true);
    try {
      const startDate = `${timeLogStart}T00:00:00`;
      const endDate = `${timeLogEnd}T23:59:59`;
      const [logs, total] = await Promise.all([
        timeLogService.getByUser(user.id),
        timeLogService.getTotalByUser(user.id, { startDate, endDate }),
      ]);
      setTimeLogs(Array.isArray(logs) ? logs : []);
      setTimeLogTotal(Number(total || 0));
    } catch (error) {
      toast.error('Failed to load time logs');
    } finally {
      setTimeLogLoading(false);
    }
  };

  const getRoleBadgeVariant = (role: string): 'warning' | 'info' | 'success' | 'danger' | 'gray' => {
    switch (role) {
      case 'ADMIN':
        return 'danger';
      case 'SENIOR_MANAGER':
        return 'warning';
      case 'MANAGER':
        return 'info';
      case 'EMPLOYEE':
        return 'success';
      default:
        return 'gray';
    }
  };

  if (!user) {
    return (
      <div className="p-6">
        <Card className="p-12 text-center">
          <p className="text-gray-600 dark:text-gray-400">Loading profile...</p>
        </Card>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6 max-w-4xl mx-auto">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">Profile</h1>
        <p className="text-gray-600 dark:text-gray-400 mt-1">Manage your account information and security</p>
      </div>

      {/* Profile Overview Card */}
      <Card className="p-8">
        <div className="flex items-start gap-6">
          <div className="relative">
            <div className="w-24 h-24 rounded-full bg-gradient-to-br from-primary-500 to-primary-600 flex items-center justify-center text-white font-bold text-3xl flex-shrink-0 overflow-hidden">
              {previewUrl ? (
                <img src={previewUrl} alt="Profile" className="w-full h-full object-cover" />
              ) : (
                <>
                  {initials.toUpperCase()}
                </>
              )}
            </div>
            <label className="absolute bottom-0 right-0 bg-primary-500 hover:bg-primary-600 text-white p-2 rounded-full cursor-pointer transition-colors">
              {uploadProfilePicMutation.isPending ? (
                <div className="w-4 h-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
              ) : (
                <Upload className="w-4 h-4" />
              )}
              <input
                type="file"
                accept="image/*"
                className="hidden"
                disabled={uploadProfilePicMutation.isPending}
                onChange={(e) => {
                  const file = e.target.files?.[0];
                  if (file) {
                    // Validate file size (5MB limit)
                    if (file.size > 5 * 1024 * 1024) {
                      toast.error('File size exceeds 5MB limit');
                      return;
                    }
                    
                    // Show preview
                    const reader = new FileReader();
                    reader.onloadend = () => {
                      setPreviewUrl(reader.result as string);
                    };
                    reader.readAsDataURL(file);
                    
                    // Upload to backend
                    uploadProfilePicMutation.mutate(file);
                  }
                }}
              />
            </label>
          </div>
          <div className="flex-1">
            <div className="flex items-start justify-between mb-4">
              <div>
                <h2 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
                  {displayFirst} {displayLast}
                </h2>
                <p className="text-gray-600 dark:text-gray-400">{user.email}</p>
              </div>
              <div className="flex gap-2">
                <Badge variant={getRoleBadgeVariant(user.role)}>
                  {(user.role || 'user').replace('_', ' ')}
                </Badge>
                <Badge variant="info">
                  <Shield className="w-3 h-3 mr-1" />
                  Level {user.accessLevel}
                </Badge>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4 text-sm">
              {user.phoneNumber && (
                <div className="flex items-center gap-2 text-gray-600 dark:text-gray-400">
                  <Phone className="w-4 h-4" />
                  <span>{user.phoneNumber}</span>
                </div>
              )}
              {user.department && (
                <div className="flex items-center gap-2 text-gray-600 dark:text-gray-400">
                  <Building2 className="w-4 h-4" />
                  <span>{user.department}</span>
                </div>
              )}
              <div className="flex items-center gap-2 text-gray-600 dark:text-gray-400">
                <Mail className="w-4 h-4" />
                <span>{user.email}</span>
              </div>
              <div className="text-gray-500 dark:text-gray-400 text-xs">
                Member since {formatDate(user.createdAt)}
              </div>
            </div>
          </div>
        </div>
      </Card>

      {/* Edit Profile Card */}
      <Card className="p-6">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="text-xl font-semibold text-gray-900 dark:text-gray-100">Profile Information</h3>
            <p className="text-sm text-gray-600 dark:text-gray-400">Update your personal information</p>
          </div>
          {!isEditingProfile && (
            <Button
              variant="secondary"
              onClick={() => setIsEditingProfile(true)}
              icon={<UserIcon className="w-4 h-4" />}
            >
              Edit Profile
            </Button>
          )}
        </div>

        {isEditingProfile ? (
          <form onSubmit={handleProfileSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Input
                label="First Name *"
                value={profileData.firstName}
                onChange={(e) => setProfileData({ ...profileData, firstName: e.target.value })}
                required
              />
              <Input
                label="Last Name *"
                value={profileData.lastName}
                onChange={(e) => setProfileData({ ...profileData, lastName: e.target.value })}
                required
              />
            </div>

            <Input
              label="Email *"
              type="email"
              value={profileData.email}
              onChange={(e) => setProfileData({ ...profileData, email: e.target.value })}
              required
            />

            <Input
              label="Phone Number"
              type="tel"
              value={profileData.phoneNumber}
              onChange={(e) => setProfileData({ ...profileData, phoneNumber: e.target.value })}
            />

            <Input
              label="Department"
              value={profileData.department}
              onChange={(e) => setProfileData({ ...profileData, department: e.target.value })}
            />

            <div className="flex justify-end gap-3 pt-4">
              <Button
                type="button"
                variant="ghost"
                onClick={() => {
                  setIsEditingProfile(false);
                  setProfileData({
                    firstName: user.firstName || user.username || '',
                    lastName: user.lastName || '',
                    email: user.email,
                    phoneNumber: user.phoneNumber || '',
                    department: user.department || '',
                  });
                }}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                isLoading={updateProfileMutation.isPending}
                icon={<Save className="w-4 h-4" />}
              >
                Save Changes
              </Button>
            </div>
          </form>
        ) : (
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <label className="block text-gray-500 dark:text-gray-400 mb-1">First Name</label>
              <p className="font-medium text-gray-900 dark:text-gray-100">{user.firstName || user.username || '-'}</p>
            </div>
            <div>
              <label className="block text-gray-500 dark:text-gray-400 mb-1">Last Name</label>
              <p className="font-medium text-gray-900 dark:text-gray-100">{user.lastName || '-'}</p>
            </div>
            <div>
              <label className="block text-gray-500 dark:text-gray-400 mb-1">Email</label>
              <p className="font-medium text-gray-900 dark:text-gray-100">{user.email}</p>
            </div>
            <div>
              <label className="block text-gray-500 dark:text-gray-400 mb-1">Phone</label>
              <p className="font-medium text-gray-900 dark:text-gray-100">{user.phoneNumber || 'Not set'}</p>
            </div>
            <div className="col-span-2">
              <label className="block text-gray-500 dark:text-gray-400 mb-1">Department</label>
              <p className="font-medium text-gray-900 dark:text-gray-100">{user.department || 'Not set'}</p>
            </div>
          </div>
        )}
      </Card>

      {/* Change Password Card */}
      <Card className="p-6">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="text-xl font-semibold text-gray-900">Password & Security</h3>
            <p className="text-sm text-gray-600">Update your password to keep your account secure</p>
          </div>
          {!isChangingPassword && (
            <Button
              variant="secondary"
              onClick={() => setIsChangingPassword(true)}
              icon={<Lock className="w-4 h-4" />}
            >
              Change Password
            </Button>
          )}
        </div>

        {isChangingPassword ? (
          <form onSubmit={handlePasswordSubmit} className="space-y-4">
            <Input
              label="Current Password *"
              type="password"
              value={passwordData.currentPassword}
              onChange={(e) =>
                setPasswordData({ ...passwordData, currentPassword: e.target.value })
              }
              required
            />

            <Input
              label="New Password *"
              type="password"
              value={passwordData.newPassword}
              onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
              required
            />

            <Input
              label="Confirm New Password *"
              type="password"
              value={passwordData.confirmPassword}
              onChange={(e) =>
                setPasswordData({ ...passwordData, confirmPassword: e.target.value })
              }
              required
            />

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
              <p className="text-sm text-blue-800">
                Password must be at least 6 characters long and should include a mix of letters,
                numbers, and special characters for better security.
              </p>
            </div>

            <div className="flex justify-end gap-3 pt-4">
              <Button
                type="button"
                variant="ghost"
                onClick={() => {
                  setIsChangingPassword(false);
                  setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
                }}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                isLoading={changePasswordMutation.isPending}
                icon={<Lock className="w-4 h-4" />}
              >
                Change Password
              </Button>
            </div>
          </form>
        ) : (
          <div className="text-sm text-gray-600">
            <p className="mb-2">
              Your password was last updated on {formatDate(user.updatedAt || user.createdAt)}.
            </p>
            <p>
              We recommend changing your password regularly to keep your account secure.
            </p>
          </div>
        )}
      </Card>

      {/* Time Logs Card */}
      <Card className="p-6">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h3 className="text-xl font-semibold text-gray-900">Time Logs</h3>
            <p className="text-sm text-gray-600">Review your logged hours by date range</p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Input
            label="Start Date"
            type="date"
            value={timeLogStart}
            onChange={(e) => setTimeLogStart(e.target.value)}
          />
          <Input
            label="End Date"
            type="date"
            value={timeLogEnd}
            onChange={(e) => setTimeLogEnd(e.target.value)}
          />
          <div className="flex items-end">
            <Button
              variant="secondary"
              onClick={handleLoadTimeLogs}
              isLoading={timeLogLoading}
              className="w-full"
            >
              Load Time Logs
            </Button>
          </div>
        </div>

        {timeLogTotal !== null && (
          <div className="mt-4 text-sm text-gray-700">
            Total hours in range: <span className="font-semibold">{timeLogTotal.toFixed(1)}h</span>
          </div>
        )}

        <div className="mt-4 space-y-2 max-h-72 overflow-y-auto">
          {timeLogs.length > 0 ? (
            timeLogs.map((log) => (
              <div key={log.id} className="border border-gray-200 rounded-lg p-3">
                <div className="flex items-center justify-between">
                  <div className="text-sm font-medium text-gray-900">{log.hoursWorked}h</div>
                  <div className="text-xs text-gray-500">{new Date(log.workDate).toLocaleDateString()}</div>
                </div>
                {log.description && (
                  <div className="text-sm text-gray-600 mt-1">{log.description}</div>
                )}
              </div>
            ))
          ) : (
            <div className="text-sm text-gray-500">No time logs loaded.</div>
          )}
        </div>
      </Card>
    </div>
  );
};
