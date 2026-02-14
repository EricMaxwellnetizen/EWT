import React, { useState } from 'react';
import { Mail, Send, ShieldCheck } from 'lucide-react';
import { useMutation } from '@tanstack/react-query';
import { emailService, type EmailRequest } from '../../services/emailService';
import { Card } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { useAuthStore } from '../../store/authStore';
import toast from 'react-hot-toast';

export const EmailPage: React.FC = () => {
  const { hasAccessLevel } = useAuthStore();
  const [mode, setMode] = useState<'simple' | 'advanced' | 'html'>('simple');
  const [recipientEmail, setRecipientEmail] = useState('');
  const [subject, setSubject] = useState('');
  const [bodyContent, setBodyContent] = useState('');
  const [ccEmails, setCcEmails] = useState('');
  const [bccEmails, setBccEmails] = useState('');
  const [priority, setPriority] = useState('NORMAL');
  const [testRecipient, setTestRecipient] = useState('');

  const sendMutation = useMutation({
    mutationFn: async (payload: EmailRequest) => {
      if (mode === 'advanced') return emailService.sendAdvanced(payload);
      if (mode === 'html') return emailService.sendHtml(payload);
      return emailService.sendSimple(payload);
    },
    onSuccess: (response) => {
      toast.success(response.statusMessage || 'Email sent');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to send email');
    },
  });

  const healthMutation = useMutation({
    mutationFn: emailService.health,
    onSuccess: (data) => {
      toast.success(data.message || 'Email service is healthy');
    },
    onError: () => {
      toast.error('Email service health check failed');
    },
  });

  const testMutation = useMutation({
    mutationFn: emailService.testEmail,
    onSuccess: () => {
      toast.success('Test email sent');
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Test email failed');
    },
  });

  const handleSend = () => {
    if (!recipientEmail || !subject || !bodyContent) {
      toast.error('Recipient, subject, and body are required');
      return;
    }

    const payload: EmailRequest = {
      recipientEmail,
      subject,
      bodyContent,
      ccEmails: ccEmails ? ccEmails.split(',').map((e) => e.trim()).filter(Boolean) : undefined,
      bccEmails: bccEmails ? bccEmails.split(',').map((e) => e.trim()).filter(Boolean) : undefined,
      isHtmlContent: mode === 'html',
      priority,
    };

    sendMutation.mutate(payload);
  };

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Email Center</h1>
        <p className="text-gray-600 mt-1">Send system emails and verify delivery</p>
      </div>

      <Card className="p-6 space-y-4">
        <div className="flex items-center gap-3">
          <Mail className="w-6 h-6 text-primary-600" />
          <h2 className="text-xl font-semibold text-gray-900">Compose Email</h2>
        </div>

        <Select
          label="Mode"
          value={mode}
          onChange={(e) => setMode(e.target.value as 'simple' | 'advanced' | 'html')}
        >
          <option value="simple">Simple</option>
          <option value="advanced">Advanced</option>
          <option value="html">HTML</option>
        </Select>

        <Input
          label="Recipient Email"
          value={recipientEmail}
          onChange={(e) => setRecipientEmail(e.target.value)}
          placeholder="user@example.com"
        />

        <Input
          label="Subject"
          value={subject}
          onChange={(e) => setSubject(e.target.value)}
          placeholder="Email subject"
        />

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Body</label>
          <textarea
            className="w-full border border-gray-300 rounded-lg px-3 py-2 min-h-[140px]"
            value={bodyContent}
            onChange={(e) => setBodyContent(e.target.value)}
            placeholder={mode === 'html' ? '<h1>Hello</h1>' : 'Email body content'}
          />
        </div>

        {mode === 'advanced' && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Input
              label="CC Emails (comma separated)"
              value={ccEmails}
              onChange={(e) => setCcEmails(e.target.value)}
              placeholder="cc1@example.com, cc2@example.com"
            />
            <Input
              label="BCC Emails (comma separated)"
              value={bccEmails}
              onChange={(e) => setBccEmails(e.target.value)}
              placeholder="bcc@example.com"
            />
          </div>
        )}

        <Select
          label="Priority"
          value={priority}
          onChange={(e) => setPriority(e.target.value)}
        >
          <option value="HIGH">High</option>
          <option value="NORMAL">Normal</option>
          <option value="LOW">Low</option>
        </Select>

        <Button
          variant="primary"
          icon={<Send className="w-4 h-4" />}
          onClick={handleSend}
          isLoading={sendMutation.isPending}
        >
          Send Email
        </Button>
      </Card>

      <Card className="p-6 space-y-4">
        <div className="flex items-center gap-3">
          <ShieldCheck className="w-6 h-6 text-emerald-600" />
          <h2 className="text-xl font-semibold text-gray-900">Health & Tests</h2>
        </div>

        <Button
          variant="secondary"
          onClick={() => healthMutation.mutate()}
          isLoading={healthMutation.isPending}
        >
          Check Email Service Health
        </Button>

        {hasAccessLevel(5) && (
          <div className="flex items-center gap-3">
            <Input
              label="Test Recipient"
              value={testRecipient}
              onChange={(e) => setTestRecipient(e.target.value)}
              placeholder="admin@example.com"
            />
            <Button
              variant="secondary"
              onClick={() => testMutation.mutate(testRecipient)}
              isLoading={testMutation.isPending}
              disabled={!testRecipient}
            >
              Send Test Email
            </Button>
          </div>
        )}
      </Card>
    </div>
  );
};
