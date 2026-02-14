import React, { useState } from 'react';
import { ChevronRight, Users, FolderKanban, GitBranch, CheckSquare, Calendar, User } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { Badge } from '../ui/Badge';
import { formatDate } from '../../utils/helpers';

interface HierarchyViewProps {
  data: any;
  type: 'client' | 'project' | 'epic' | 'story';
  level?: number;
  onNavigate?: (type: string, id: number) => void;
}

/**
 * HierarchyView - Expandable hierarchical view component
 * Shows ownership relationships: Client → Projects → Epics → Stories → Users
 */
export const HierarchyView: React.FC<HierarchyViewProps> = ({ 
  data, 
  type, 
  level = 0,
  onNavigate 
}) => {
  const [isExpanded, setIsExpanded] = useState(false);

  const getIcon = () => {
    switch (type) {
      case 'client': return <Users className="w-5 h-5" />;
      case 'project': return <FolderKanban className="w-5 h-5" />;
      case 'epic': return <GitBranch className="w-5 h-5" />;
      case 'story': return <CheckSquare className="w-5 h-5" />;
      default: return null;
    }
  };

  const getChildrenData = () => {
    switch (type) {
      case 'client':
        return { children: data.projects || [], childType: 'project', label: 'Projects' };
      case 'project':
        return { children: data.epics || [], childType: 'epic', label: 'Epics' };
      case 'epic':
        return { children: data.stories || [], childType: 'story', label: 'Stories' };
      case 'story':
        return { children: [], childType: null, label: null };
      default:
        return { children: [], childType: null, label: null };
    }
  };

  const { children, childType, label } = getChildrenData();
  const hasChildren = children && children.length > 0;

  const containerVariants = {
    hidden: { opacity: 0, height: 0 },
    visible: { 
      opacity: 1, 
      height: 'auto',
      transition: {
        height: { duration: 0.3 },
        opacity: { duration: 0.2, delay: 0.1 }
      }
    },
    exit: { 
      opacity: 0, 
      height: 0,
      transition: {
        height: { duration: 0.3, delay: 0.1 },
        opacity: { duration: 0.2 }
      }
    }
  };

  const itemVariants = {
    hidden: { opacity: 0, x: -20 },
    visible: (i: number) => ({
      opacity: 1,
      x: 0,
      transition: { delay: i * 0.05 }
    })
  };

  const getStatusBadge = () => {
    if (type === 'project' || type === 'epic' || type === 'story') {
      const isApproved = data.isApproved || data.is_approved || data.isCompleted;
      return (
        <Badge variant={isApproved ? 'success' : 'warning'}>
          {isApproved ? 'Completed' : 'Active'}
        </Badge>
      );
    }
    return null;
  };

  return (
    <div className={`${level > 0 ? 'ml-6 border-l-2 border-primary-200 pl-4' : ''}`}>
      {/* Main Item */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className={`
          group relative bg-white rounded-lg shadow-sm hover:shadow-md 
          transition-all duration-200 mb-3
          ${hasChildren ? 'cursor-pointer' : ''}
        `}
        onClick={() => hasChildren && setIsExpanded(!isExpanded)}
      >
        <div className="p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3 flex-1">
              {/* Expand/Collapse Icon */}
              {hasChildren && (
                <motion.div
                  animate={{ rotate: isExpanded ? 90 : 0 }}
                  transition={{ duration: 0.2 }}
                  className="text-primary-600"
                >
                  <ChevronRight className="w-5 h-5" />
                </motion.div>
              )}
              
              {/* Type Icon */}
              <div className={`
                p-2 rounded-lg
                ${type === 'client' ? 'bg-purple-100 text-purple-600' : ''}
                ${type === 'project' ? 'bg-blue-100 text-blue-600' : ''}
                ${type === 'epic' ? 'bg-green-100 text-green-600' : ''}
                ${type === 'story' ? 'bg-orange-100 text-orange-600' : ''}
              `}>
                {getIcon()}
              </div>

              {/* Name & Details */}
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <h3 className="font-semibold text-gray-900 truncate">
                    {data.name || data.title}
                  </h3>
                  {getStatusBadge()}
                </div>
                <p className="text-sm text-gray-600 truncate">
                  {data.description || data.deliverables || data.email}
                </p>
              </div>
            </div>

            {/* Metadata */}
            <div className="flex items-center gap-4 ml-4">
              {/* Manager/Assigned User */}
              {(data.manager_id || data.assigned_to || data.assignedToUser) && (
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <User className="w-4 h-4" />
                  <span className="hidden md:inline">
                    {data.manager_id?.firstName || data.assigned_to?.firstName || data.assignedToUser?.firstName}
                  </span>
                </div>
              )}

              {/* Deadline/Due Date */}
              {(data.deadline || data.dueDate || data.endDate) && (
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <Calendar className="w-4 h-4" />
                  <span className="hidden md:inline">
                    {formatDate(data.deadline || data.dueDate || data.endDate)}
                  </span>
                </div>
              )}

              {/* Child Count Badge */}
              {hasChildren && (
                <Badge variant="info">
                  {children.length} {label}
                </Badge>
              )}
            </div>
          </div>

          {/* Progress Bar for Projects/Epics */}
          {(type === 'project' || type === 'epic') && data.completionRate !== undefined && (
            <div className="mt-3">
              <div className="flex items-center justify-between mb-1">
                <span className="text-xs text-gray-500">Progress</span>
                <span className="text-xs font-medium text-gray-700">{data.completionRate}%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <motion.div
                  initial={{ width: 0 }}
                  animate={{ width: `${data.completionRate}%` }}
                  transition={{ duration: 0.5, delay: 0.2 }}
                  className="bg-gradient-to-r from-primary-500 to-primary-600 h-2 rounded-full"
                />
              </div>
            </div>
          )}
        </div>

        {/* Hover Effect Border */}
        <div className={`
          absolute inset-0 rounded-lg border-2 border-transparent
          group-hover:border-primary-300 transition-colors duration-200
          pointer-events-none
        `} />
      </motion.div>

      {/* Expanded Children */}
      <AnimatePresence>
        {isExpanded && hasChildren && (
          <motion.div
            variants={containerVariants}
            initial="hidden"
            animate="visible"
            exit="exit"
            className="overflow-hidden"
          >
            <div className="space-y-2 mt-2">
              {children.map((child: any, index: number) => (
                <motion.div
                  key={child.id || child.projectId || child.epicId || child.storyId}
                  custom={index}
                  variants={itemVariants}
                  initial="hidden"
                  animate="visible"
                >
                  <HierarchyView
                    data={child}
                    type={childType as any}
                    level={level + 1}
                    onNavigate={onNavigate}
                  />
                </motion.div>
              ))}
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Story Assigned Users - Special Case */}
      {type === 'story' && isExpanded && data.assigned_to && (
        <motion.div
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          exit={{ opacity: 0, height: 0 }}
          className="ml-6 mt-2 p-3 bg-gray-50 rounded-lg border border-gray-200"
        >
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary-500 to-primary-600 flex items-center justify-center text-white font-semibold">
              {data.assigned_to.firstName?.[0]}{data.assigned_to.lastName?.[0]}
            </div>
            <div>
              <p className="font-medium text-gray-900">
                {data.assigned_to.firstName} {data.assigned_to.lastName}
              </p>
              <p className="text-sm text-gray-600">{data.assigned_to.email}</p>
            </div>
            <Badge variant="info" className="ml-auto">
              {data.assigned_to.role}
            </Badge>
          </div>
        </motion.div>
      )}
    </div>
  );
};
