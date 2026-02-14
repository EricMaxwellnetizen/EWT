import React, { useState } from 'react';
import { Clock, Plus, Trash2, Edit } from 'lucide-react';
import { timeLogService } from '../../services/timeLogService';
import type { TimeLog } from '../../types';
import toast from 'react-hot-toast';

interface TimeTrackingProps {
  storyId: number;
}

export const TimeTracking: React.FC<TimeTrackingProps> = ({ storyId }) => {
  const [timeLogs, setTimeLogs] = useState<TimeLog[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [totalHours, setTotalHours] = useState(0);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formData, setFormData] = useState({
    hoursWorked: '',
    description: '',
    workDate: new Date().toISOString().split('T')[0],
  });
  const [loading, setLoading] = useState(false);

  React.useEffect(() => {
    loadTimeLogs();
  }, [storyId]);

  const loadTimeLogs = async () => {
    try {
      const [logs, total] = await Promise.all([
        timeLogService.getByStory(storyId),
        timeLogService.getTotalByStory(storyId),
      ]);
      setTimeLogs(logs || []);
      setTotalHours(Number(total || 0));
    } catch (err) {
      console.error('Error loading time logs:', err);
      toast.error('Failed to load time logs');
    }
  };

  const handleSubmit = async () => {
    if (!formData.hoursWorked) return;

    setLoading(true);
    try {
      const payload = {
        storyId,
        hoursWorked: parseFloat(formData.hoursWorked),
        description: formData.description,
        workDate: formData.workDate.includes('T') ? formData.workDate : `${formData.workDate}T00:00:00`,
      };

      if (editingId) {
        await timeLogService.update(editingId, payload);
        toast.success('Time log updated');
      } else {
        await timeLogService.logTime(payload);
        toast.success('Time log added');
      }

      setFormData({ hoursWorked: '', description: '', workDate: new Date().toISOString().split('T')[0] });
      setShowForm(false);
      setEditingId(null);
      await loadTimeLogs();
    } catch (err) {
      console.error('Error logging time:', err);
      toast.error(editingId ? 'Failed to update time log' : 'Failed to log time');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await timeLogService.delete(id);
      await loadTimeLogs();
      toast.success('Time log deleted');
    } catch (err) {
      console.error('Error deleting time log:', err);
      toast.error('Failed to delete time log');
    }
  };

  const handleEdit = (log: any) => {
    setEditingId(log.id);
    setFormData({
      hoursWorked: String(log.hoursWorked ?? ''),
      description: log.description ?? '',
      workDate: log.date ? new Date(log.date).toISOString().split('T')[0] : new Date().toISOString().split('T')[0],
    });
    setShowForm(true);
  };

  return (
    <div className="bg-white rounded-lg border border-gray-200 p-6">
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-2">
          <Clock className="w-5 h-5 text-blue-600" />
          <h3 className="text-lg font-semibold text-gray-900">Time Tracking</h3>
        </div>
        <div className="flex items-center gap-4">
          <div className="text-right">
            <p className="text-sm text-gray-600">Total Hours Logged</p>
            <p className="text-2xl font-bold text-blue-600">{totalHours.toFixed(1)}h</p>
          </div>
          <button
            onClick={() => setShowForm(!showForm)}
            className="flex items-center gap-2 px-3 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition"
          >
            <Plus className="w-4 h-4" />
            Log Time
          </button>
        </div>
      </div>

      {showForm && (
        <div className="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Hours Worked *
              </label>
              <input
                type="number"
                step="0.5"
                min="0"
                value={formData.hoursWorked}
                onChange={(e) => setFormData({ ...formData, hoursWorked: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="e.g., 2.5"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Work Date
              </label>
              <input
                type="date"
                value={formData.workDate}
                onChange={(e) => setFormData({ ...formData, workDate: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Description
              </label>
              <textarea
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="What did you work on?"
                rows={3}
              />
            </div>
            <div className="flex gap-2">
              <button
                onClick={handleSubmit}
                disabled={loading || !formData.hoursWorked}
                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:bg-gray-400 transition"
              >
                {loading ? 'Saving...' : editingId ? 'Update Log' : 'Log Time'}
              </button>
              <button
                onClick={() => {
                  setShowForm(false);
                  setEditingId(null);
                  setFormData({ hoursWorked: '', description: '', workDate: new Date().toISOString().split('T')[0] });
                }}
                className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded hover:bg-gray-50 transition"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="space-y-2 max-h-96 overflow-y-auto">
        {timeLogs.length > 0 ? (
          timeLogs.map((log) => (
            <div key={log.id!} className="p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <p className="font-medium text-gray-900">{log.hoursWorked} hours</p>
                  <p className="text-sm text-gray-600 mt-1">{log.description}</p>
                  <p className="text-xs text-gray-500 mt-2">
                    {new Date(log.date).toLocaleDateString()}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => handleEdit(log)}
                    className="p-2 text-blue-600 hover:bg-blue-50 rounded transition"
                  >
                    <Edit className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => handleDelete(log.id!)}
                    className="p-2 text-red-600 hover:bg-red-50 rounded transition"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="text-center py-8 text-gray-500">
            <Clock className="w-8 h-8 mx-auto mb-2 opacity-50" />
            <p>No time logs yet</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default TimeTracking;
