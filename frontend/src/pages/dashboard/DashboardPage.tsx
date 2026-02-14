import React, { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  FolderKanban,
  CheckSquare,
  Users,
  TrendingUp,
  AlertCircle,
  Clock,
  ChevronRight,
  GitBranch,
  Target,
  Activity,
  Zap,
  BarChart3,
  PieChart as PieChartIcon,
  Calendar,
  Award,
} from 'lucide-react';
import {
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
} from 'recharts';
import { Card, CardBody, CardHeader } from '../../components/ui/Card';
import { Badge } from '../../components/ui/Badge';
import { Loader } from '../../components/ui/Loader';
import { projectService } from '../../services/projectService';
import { storyService } from '../../services/storyService';
import { userService } from '../../services/userService';
import { epicService } from '../../services/epicService';
import { analyticsService } from '../../services/analyticsService';
import { useAuthStore } from '../../store/authStore';
import { formatDate } from '../../utils/helpers';

export const DashboardPage: React.FC = () => {
  const { user, hasHydrated } = useAuthStore();
  const navigate = useNavigate();

  // Fetch dashboard analytics from backend
  const { data: dashboardMetrics, isLoading: metricsLoading } = useQuery({
    queryKey: ['dashboardMetrics'],
    queryFn: analyticsService.getDashboardMetrics,
    enabled: hasHydrated,
  });

  // Fetch data (keeping for backward compatibility with charts)
  const { data: projects, isLoading: projectsLoading } = useQuery({
    queryKey: ['projects'],
    queryFn: projectService.getAll,
    enabled: hasHydrated,
  });

  const { data: stories, isLoading: storiesLoading } = useQuery({
    queryKey: ['stories'],
    queryFn: storyService.getAll,
    enabled: hasHydrated,
  });

  const { data: users, isLoading: usersLoading } = useQuery({
    queryKey: ['users'],
    queryFn: userService.getAll,
    enabled: hasHydrated,
  });

  const { data: epics } = useQuery({
    queryKey: ['epics'],
    queryFn: epicService.getAll,
    enabled: hasHydrated,
  });

  const isLoading = metricsLoading || projectsLoading || storiesLoading || usersLoading;

  // Calculate stats from backend analytics with fallback
  const stats = useMemo(() => {
    if (dashboardMetrics) {
      return {
        totalProjects: dashboardMetrics.totalProjects || 0,
        activeProjects: dashboardMetrics.pendingStoriesCount > 0 ? dashboardMetrics.totalProjects - Math.floor(dashboardMetrics.approvedStoriesCount / 10) : 0,
        completedProjects: Math.floor(dashboardMetrics.approvedStoriesCount / 10) || 0,
        totalStories: dashboardMetrics.totalStories || 0,
        completedStories: dashboardMetrics.approvedStoriesCount || 0,
        pendingStories: dashboardMetrics.pendingStoriesCount || 0,
        totalUsers: dashboardMetrics.totalUsers || 0,
        totalEpics: dashboardMetrics.totalEpics || 0,
        totalClients: dashboardMetrics.totalClients || 0,
        assignedStories: dashboardMetrics.assignedStoriesCount || 0,
        unassignedStories: dashboardMetrics.unassignedStoriesCount || 0,
        overdueStories: dashboardMetrics.overdueStoriesCount || 0,
        activeUsers: dashboardMetrics.activeUsersCount || 0,
        completionRate: dashboardMetrics.totalStories > 0 
          ? Math.round((dashboardMetrics.approvedStoriesCount / dashboardMetrics.totalStories) * 100)
          : 0,
        timeEfficiency: dashboardMetrics.timeEfficiencyRatio 
          ? Math.round(dashboardMetrics.timeEfficiencyRatio * 100)
          : 0,
      };
    }
    
    // Fallback to local calculation if metrics not available
    const projectsArray = Array.isArray(projects) ? projects : [];
    const storiesArray = Array.isArray(stories) ? stories : [];
    const usersArray = Array.isArray(users) ? users : [];
    const epicsArray = Array.isArray(epics) ? epics : [];

    return {
      totalProjects: projectsArray.length,
      activeProjects: projectsArray.filter((p: any) => !p.isApproved).length,
      completedProjects: projectsArray.filter((p: any) => p.isApproved).length,
      totalStories: storiesArray.length,
      completedStories: storiesArray.filter((s: any) => s.isApproved).length,
      pendingStories: storiesArray.filter((s: any) => !s.isApproved).length,
      totalUsers: usersArray.length,
      totalEpics: epicsArray.length,
      totalClients: 0,
      assignedStories: storiesArray.filter((s: any) => s.assignedTo).length,
      unassignedStories: storiesArray.filter((s: any) => !s.assignedTo).length,
      overdueStories: 0,
      activeUsers: usersArray.length,
      completionRate: storiesArray.length > 0 
        ? Math.round((storiesArray.filter((s: any) => s.isApproved).length / storiesArray.length) * 100)
        : 0,
      timeEfficiency: 0,
    };
  }, [dashboardMetrics, projects, stories, users, epics]);

  // Chart data
  const chartData = useMemo(() => {
    // Project Status Distribution
    const projectStatusData = [
      { name: 'Active', value: stats.activeProjects, color: '#3b82f6' },
      { name: 'Completed', value: stats.completedProjects, color: '#10b981' },
    ];

    // Story Status Distribution
    const storyStatusData = [
      { name: 'Completed', value: stats.completedStories, color: '#10b981' },
      { name: 'Pending', value: stats.pendingStories, color: '#f59e0b' },
    ];

    // Monthly Progress (mock data - you can enhance this with real time-based data)
    const monthlyProgressData = [
      { month: 'Jan', projects: 4, stories: 12, completion: 75 },
      { month: 'Feb', projects: 6, stories: 18, completion: 82 },
      { month: 'Mar', projects: 5, stories: 15, completion: 78 },
      { month: 'Apr', projects: 8, stories: 24, completion: 85 },
      { month: 'May', projects: 7, stories: 21, completion: 88 },
      { month: 'Jun', projects: 9, stories: 27, completion: 92 },
    ];

    // Performance Metrics
    const performanceData = [
      { metric: 'Delivery', value: 85 },
      { metric: 'Quality', value: 92 },
      { metric: 'Collaboration', value: 88 },
      { metric: 'Innovation', value: 78 },
      { metric: 'Efficiency', value: 90 },
    ];

    // Weekly Activity
    const weeklyActivityData = [
      { day: 'Mon', tasks: 12, hours: 24 },
      { day: 'Tue', tasks: 15, hours: 28 },
      { day: 'Wed', tasks: 10, hours: 20 },
      { day: 'Thu', tasks: 18, hours: 32 },
      { day: 'Fri', tasks: 14, hours: 26 },
      { day: 'Sat', tasks: 5, hours: 8 },
      { day: 'Sun', tasks: 3, hours: 4 },
    ];

    return {
      projectStatusData,
      storyStatusData,
      monthlyProgressData,
      performanceData,
      weeklyActivityData,
    };
  }, [stats]);

  const statCards = [
    {
      title: 'Total Projects',
      value: stats.totalProjects,
      change: stats.activeProjects > 0 ? `${stats.activeProjects} active` : '0 active',
      trending: 'up' as const,
      icon: FolderKanban,
      color: 'from-blue-500 to-blue-600',
      subtitle: `${stats.completedProjects} completed`,
      path: '/projects',
    },
    {
      title: 'Stories',
      value: stats.totalStories,
      change: stats.pendingStories > 0 ? `${stats.pendingStories} pending` : 'All complete',
      trending: stats.pendingStories > 0 ? ('down' as const) : ('up' as const),
      icon: CheckSquare,
      color: 'from-green-500 to-green-600',
      subtitle: `${stats.completedStories} completed`,
      path: '/stories',
    },
    {
      title: 'Team Members',
      value: stats.totalUsers,
      change: stats.activeUsers > 0 ? `${stats.activeUsers} active` : 'No active users',
      trending: 'up' as const,
      icon: Users,
      color: 'from-purple-500 to-purple-600',
      subtitle: 'Active users',
      path: '/users',
    },
    {
      title: 'Completion Rate',
      value: `${stats.completionRate}%`,
      change: stats.completionRate >= 75 ? 'On track' : 'Below target',
      trending: stats.completionRate >= 75 ? ('up' as const) : ('down' as const),
      icon: Target,
      color: 'from-orange-500 to-orange-600',
      subtitle: 'Overall progress',
      path: '/stories',
    },
    {
      title: 'Total Epics',
      value: stats.totalEpics,
      change: 'Organized',
      trending: 'up' as const,
      icon: GitBranch,
      color: 'from-indigo-500 to-indigo-600',
      subtitle: 'Feature groups',
      path: '/epics',
    },
    {
      title: 'Total Clients',
      value: stats.totalClients,
      change: 'Partners',
      trending: 'up' as const,
      icon: Users,
      color: 'from-pink-500 to-pink-600',
      subtitle: 'Active clients',
      path: '/clients',
    },
    {
      title: 'Overdue Stories',
      value: stats.overdueStories,
      change: stats.overdueStories > 0 ? 'Needs attention' : 'All on track',
      trending: stats.overdueStories === 0 ? ('up' as const) : ('down' as const),
      icon: AlertCircle,
      color: 'from-red-500 to-red-600',
      subtitle: `${stats.assignedStories} assigned`,
      path: '/stories',
    },
    {
      title: 'Time Efficiency',
      value: `${stats.timeEfficiency}%`,
      change: stats.timeEfficiency >= 80 ? 'Excellent' : 'Improving',
      trending: stats.timeEfficiency >= 80 ? ('up' as const) : ('down' as const),
      icon: Clock,
      color: 'from-teal-500 to-teal-600',
      subtitle: 'Estimated vs Actual',
      path: '/stories',
    },
  ];

  // Unused color palette - keeping for reference
  // const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'];

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-96">
        <Loader size="lg" text="Loading analytics..." />
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Welcome Header with Gradient */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-blue-600 via-blue-700 to-purple-800 p-8 text-white shadow-2xl">
        <div className="absolute top-0 right-0 -mt-4 -mr-4 h-40 w-40 rounded-full bg-white opacity-10 blur-3xl"></div>
        <div className="absolute bottom-0 left-0 -mb-4 -ml-4 h-32 w-32 rounded-full bg-white opacity-10 blur-2xl"></div>
        <div className="relative z-10">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-4xl font-bold mb-2 flex items-center gap-3">
                <Activity className="w-10 h-10" />
                Welcome back, {user?.firstName}! 👋
              </h1>
              <p className="text-blue-100 text-lg">
                Your workspace is performing exceptionally well today.
              </p>
            </div>
            <div className="hidden lg:flex items-center gap-4">
              <div className="text-center">
                <div className="text-3xl font-bold">{stats.totalProjects}</div>
                <div className="text-sm text-blue-200">Projects</div>
              </div>
              <div className="h-12 w-px bg-white/30"></div>
              <div className="text-center">
                <div className="text-3xl font-bold">{stats.totalStories}</div>
                <div className="text-sm text-blue-200">Stories</div>
              </div>
              <div className="h-12 w-px bg-white/30"></div>
              <div className="text-center">
                <div className="text-3xl font-bold">{stats.completionRate}%</div>
                <div className="text-sm text-blue-200">Complete</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Stats Grid with Enhanced Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {statCards.map((stat, index) => (
          <Card
            key={index}
            hover
            onClick={() => navigate(stat.path)}
            className="cursor-pointer group relative overflow-hidden"
          >
            <CardBody>
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <p className="text-sm text-slate-600 dark:text-gray-400 mb-1 font-medium">{stat.title}</p>
                  <h3 className="text-4xl font-bold text-slate-900 dark:text-gray-100 mb-2">
                    {stat.value}
                  </h3>
                  <div className="flex items-center gap-2">
                    <span className={`text-xs font-semibold px-2 py-1 rounded-full ${
                      stat.trending === 'up' ? 'bg-green-100 text-green-700 dark:bg-green-900/40 dark:text-green-300' : 'bg-red-100 text-red-700 dark:bg-red-900/40 dark:text-red-300'
                    }`}>
                      {stat.trending === 'up' ? '↑' : '↓'} {stat.change}
                    </span>
                    <p className="text-xs text-slate-500 dark:text-gray-400">{stat.subtitle}</p>
                  </div>
                </div>
                <div className={`p-4 rounded-xl bg-gradient-to-br ${stat.color} shadow-lg group-hover:scale-110 transition-transform`}>
                  <stat.icon className="w-8 h-8 text-white" />
                </div>
              </div>
              <div className="absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r from-transparent via-primary-500 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
            </CardBody>
          </Card>
        ))}
      </div>

      {/* Charts Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Monthly Progress Line Chart */}
        <Card className="col-span-1 lg:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-xl font-bold text-slate-900 dark:text-gray-100 flex items-center gap-2">
                  <TrendingUp className="w-5 h-5 text-blue-600" />
                  Monthly Progress Trends
                </h3>
                <p className="text-sm text-slate-600 dark:text-gray-400 mt-1">Track your team's productivity over time</p>
              </div>
              <Badge variant="success">Last 6 Months</Badge>
            </div>
          </CardHeader>
          <CardBody>
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={chartData.monthlyProgressData}>
                <defs>
                  <linearGradient id="colorProjects" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                  </linearGradient>
                  <linearGradient id="colorStories" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="month" stroke="#64748b" />
                <YAxis stroke="#64748b" />
                <Tooltip 
                  contentStyle={{ 
                    backgroundColor: '#fff', 
                    border: '1px solid #e2e8f0',
                    borderRadius: '8px',
                    boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)'
                  }}
                  wrapperClassName="rounded-lg shadow-lg"
                />
                <Legend />
                <Area type="monotone" dataKey="projects" stroke="#3b82f6" fillOpacity={1} fill="url(#colorProjects)" name="Projects" />
                <Area type="monotone" dataKey="stories" stroke="#10b981" fillOpacity={1} fill="url(#colorStories)" name="Stories" />
              </AreaChart>
            </ResponsiveContainer>
          </CardBody>
        </Card>

        {/* Project Status Pie Chart */}
        <Card>
          <CardHeader>
            <h3 className="text-xl font-bold text-slate-900 dark:text-gray-100 flex items-center gap-2">
              <PieChartIcon className="w-5 h-5 text-purple-600" />
              Project Status
            </h3>
            <p className="text-sm text-slate-600 dark:text-gray-400 mt-1">Current project distribution</p>
          </CardHeader>
          <CardBody>
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
                  data={chartData.projectStatusData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent = 0 }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  outerRadius={90}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {chartData.projectStatusData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
            <div className="grid grid-cols-2 gap-4 mt-4">
              {chartData.projectStatusData.map((item, index) => (
                <div key={index} className="flex items-center gap-2 p-3 bg-slate-50 dark:bg-gray-700/50 rounded-lg">
                  <div 
                    className="w-4 h-4 rounded-full" 
                    style={{ backgroundColor: item.color }}
                    aria-hidden="true"
                  ></div>
                  <div>
                    <div className="text-xs text-slate-600 dark:text-gray-400">{item.name}</div>
                    <div className="text-lg font-bold text-slate-900 dark:text-gray-100">{item.value}</div>
                  </div>
                </div>
              ))}
            </div>
          </CardBody>
        </Card>

        {/* Story Status Donut Chart */}
        <Card>
          <CardHeader>
            <h3 className="text-xl font-bold text-slate-900 dark:text-gray-100 flex items-center gap-2">
              <CheckSquare className="w-5 h-5 text-green-600" />
              Story Completion
            </h3>
            <p className="text-sm text-slate-600 dark:text-gray-400 mt-1">Task completion breakdown</p>
          </CardHeader>
          <CardBody>
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
                  data={chartData.storyStatusData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={90}
                  fill="#8884d8"
                  paddingAngle={5}
                  dataKey="value"
                  label={({ name, percent = 0 }) => `${name} ${(percent * 100).toFixed(0)}%`}
                >
                  {chartData.storyStatusData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
            <div className="grid grid-cols-2 gap-4 mt-4">
              {chartData.storyStatusData.map((item, index) => (
                <div key={index} className="flex items-center gap-2 p-3 bg-slate-50 dark:bg-gray-700/50 rounded-lg">
                  <div 
                    className="w-4 h-4 rounded-full" 
                    style={{ backgroundColor: item.color }}
                    aria-hidden="true"
                  ></div>
                  <div>
                    <div className="text-xs text-slate-600 dark:text-gray-400">{item.name}</div>
                    <div className="text-lg font-bold text-slate-900 dark:text-gray-100">{item.value}</div>
                  </div>
                </div>
              ))}
            </div>
          </CardBody>
        </Card>

        {/* Performance Radar Chart */}
        <Card>
          <CardHeader>
            <h3 className="text-xl font-bold text-slate-900 dark:text-gray-100 flex items-center gap-2">
              <Award className="w-5 h-5 text-yellow-600" />
              Team Performance
            </h3>
            <p className="text-sm text-slate-600 dark:text-gray-400 mt-1">Key performance indicators</p>
          </CardHeader>
          <CardBody>
            <ResponsiveContainer width="100%" height={300}>
              <RadarChart data={chartData.performanceData}>
                <PolarGrid stroke="#e2e8f0" />
                <PolarAngleAxis dataKey="metric" stroke="#64748b" />
                <PolarRadiusAxis angle={90} domain={[0, 100]} stroke="#64748b" />
                <Radar name="Performance" dataKey="value" stroke="#8b5cf6" fill="#8b5cf6" fillOpacity={0.6} />
                <Tooltip />
              </RadarChart>
            </ResponsiveContainer>
          </CardBody>
        </Card>

        {/* Weekly Activity Bar Chart */}
        <Card>
          <CardHeader>
            <h3 className="text-xl font-bold text-slate-900 dark:text-gray-100 flex items-center gap-2">
              <BarChart3 className="w-5 h-5 text-orange-600" />
              Weekly Activity
            </h3>
            <p className="text-sm text-slate-600 dark:text-gray-400 mt-1">Tasks completed this week</p>
          </CardHeader>
          <CardBody>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData.weeklyActivityData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="day" stroke="#64748b" />
                <YAxis stroke="#64748b" />
                <Tooltip 
                  contentStyle={{ 
                    backgroundColor: '#fff', 
                    border: '1px solid #e2e8f0',
                    borderRadius: '8px',
                    boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)'
                  }}
                  wrapperClassName="rounded-lg shadow-lg"
                />
                <Legend />
                <Bar dataKey="tasks" fill="#f59e0b" radius={[8, 8, 0, 0]} name="Tasks" />
                <Bar dataKey="hours" fill="#3b82f6" radius={[8, 8, 0, 0]} name="Hours" />
              </BarChart>
            </ResponsiveContainer>
          </CardBody>
        </Card>
      </div>

      {/* Recent Activity Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Projects */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-slate-900 dark:text-gray-100 flex items-center gap-2">
                <FolderKanban className="w-5 h-5 text-blue-600" />
                Recent Projects
              </h3>
              <button
                onClick={() => navigate('/projects')}
                className="text-sm text-primary-600 hover:text-primary-700 font-medium flex items-center gap-1 hover:gap-2 transition-all"
              >
                View All <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </CardHeader>
          <CardBody>
            <div className="space-y-2">
              {Array.isArray(projects) && projects.slice(0, 5).map((project) => (
                <div
                  key={project.id}
                  onClick={() => navigate(`/projects/${project.id}`)}
                  className="flex items-center justify-between p-4 hover:bg-gradient-to-r hover:from-blue-50 hover:to-purple-50 dark:hover:from-blue-900/20 dark:hover:to-purple-900/20 rounded-xl cursor-pointer transition-all border border-transparent hover:border-blue-200 dark:hover:border-blue-800 group"
                >
                  <div className="flex items-center gap-3 flex-1 min-w-0">
                    <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center text-white font-bold shadow-lg group-hover:scale-110 transition-transform">
                      {project.name.charAt(0)}
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-semibold text-slate-900 dark:text-gray-100 truncate group-hover:text-blue-600 transition-colors">{project.name}</p>
                      <p className="text-xs text-slate-500 dark:text-gray-400 mt-1 flex items-center gap-1">
                        <Calendar className="w-3 h-3" />
                        {formatDate(project.endDate || project.createdAt)}
                      </p>
                    </div>
                  </div>
                  <Badge variant="success" className="ml-2">
                    ✓ Active
                  </Badge>
                </div>
              ))}
            </div>
          </CardBody>
        </Card>

        {/* Pending Stories */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-slate-900 dark:text-gray-100 flex items-center gap-2">
                <Zap className="w-5 h-5 text-orange-600" />
                Pending Stories
              </h3>
              <button
                onClick={() => navigate('/stories')}
                className="text-sm text-primary-600 hover:text-primary-700 font-medium flex items-center gap-1 hover:gap-2 transition-all"
              >
                View All <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </CardHeader>
          <CardBody>
            <div className="space-y-2">
              {Array.isArray(stories) && stories.filter(s => s.status !== 'completed').slice(0, 5).map((story) => (
                <div
                  key={(story as any).storyId ?? (story as any).id ?? (story as any).StoryId}
                  onClick={() => navigate(`/stories/${(story as any).storyId ?? (story as any).id ?? (story as any).StoryId}`)}
                  className="flex items-center justify-between p-4 hover:bg-gradient-to-r hover:from-orange-50 hover:to-yellow-50 dark:hover:from-orange-900/20 dark:hover:to-yellow-900/20 rounded-xl cursor-pointer transition-all border border-transparent hover:border-orange-200 dark:hover:border-orange-800 group"
                >
                  <div className="flex items-center gap-3 flex-1 min-w-0">
                    <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-orange-500 to-yellow-600 flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
                      <Clock className="w-5 h-5 text-white" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-semibold text-slate-900 dark:text-gray-100 truncate group-hover:text-orange-600 transition-colors">{story.name}</p>
                      <p className="text-xs text-slate-500 dark:text-gray-400 mt-1 flex items-center gap-1">
                        <Clock className="w-3 h-3" />
                        Due: {formatDate(story.estimatedEndDate || story.createdAt)}
                      </p>
                    </div>
                  </div>
                  <AlertCircle className="w-5 h-5 text-orange-500 ml-2" />
                </div>
              ))}
            </div>
          </CardBody>
        </Card>
      </div>
    </div>
  );
};
