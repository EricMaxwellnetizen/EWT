import React, { useEffect } from 'react';
import useWebSocket from '../../hooks/useWebSocket';
import { Bell, CheckCircle, AlertCircle, Edit3, Plus } from 'lucide-react';

interface RealtimeActivityProps {
  onNewActivity?: (activity: any) => void;
}

export const RealtimeActivity: React.FC<RealtimeActivityProps> = ({ onNewActivity }) => {
  const { connected, messages, error } = useWebSocket('ws://localhost:8082/ws');
  const [displayedMessages, setDisplayedMessages] = React.useState<any[]>([]);

  useEffect(() => {
    if (messages.length > 0) {
      const latestMessage = messages[0];
      setDisplayedMessages((prev) => [latestMessage, ...prev.slice(0, 9)]);
      
      if (onNewActivity) {
        onNewActivity(latestMessage);
      }
    }
  }, [messages, onNewActivity]);

  const getMessageIcon = (type: string) => {
    switch (type) {
      case 'STORY_CREATED':
        return <Plus className="w-4 h-4 text-green-600" />;
      case 'STORY_UPDATED':
        return <Edit3 className="w-4 h-4 text-blue-600" />;
      case 'NOTIFICATION_CREATED':
        return <Bell className="w-4 h-4 text-yellow-600" />;
      case 'PROJECT_UPDATED':
        return <CheckCircle className="w-4 h-4 text-purple-600" />;
      default:
        return <AlertCircle className="w-4 h-4 text-gray-600" />;
    }
  };

  const getMessageColor = (type: string) => {
    switch (type) {
      case 'STORY_CREATED':
        return 'bg-green-50 border-green-200';
      case 'STORY_UPDATED':
        return 'bg-blue-50 border-blue-200';
      case 'NOTIFICATION_CREATED':
        return 'bg-yellow-50 border-yellow-200';
      case 'PROJECT_UPDATED':
        return 'bg-purple-50 border-purple-200';
      default:
        return 'bg-gray-50 border-gray-200';
    }
  };

  return (
    <div className="w-full">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-900">Real-time Activity</h3>
        <div className="flex items-center gap-2">
          <div className={`w-2 h-2 rounded-full ${connected ? 'bg-green-500' : 'bg-red-500'}`} />
          <span className="text-xs text-gray-600">
            {connected ? 'Live' : 'Offline'}
          </span>
        </div>
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded text-sm text-red-700">
          Connection error: {error}
        </div>
      )}

      <div className="space-y-2 max-h-96 overflow-y-auto">
        {displayedMessages.length > 0 ? (
          displayedMessages.map((msg, index) => (
            <div
              key={index}
              className={`p-3 border rounded-lg flex items-start gap-3 text-sm transition-all ${getMessageColor(msg.type)}`}
            >
              <div className="mt-0.5">
                {getMessageIcon(msg.type)}
              </div>
              <div className="flex-1 min-w-0">
                <p className="font-medium text-gray-900 truncate">
                  {msg.userName || 'System'}: {msg.message}
                </p>
                <p className="text-xs text-gray-600 mt-1">
                  {msg.entityType} • {new Date(msg.timestamp).toLocaleTimeString()}
                </p>
              </div>
            </div>
          ))
        ) : (
          <div className="text-center py-8 text-gray-500">
            <Bell className="w-8 h-8 mx-auto mb-2 opacity-50" />
            <p>No recent activity</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default RealtimeActivity;
