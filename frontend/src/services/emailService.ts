import apiClient, { get, post } from '../lib/api';
import { ENDPOINTS } from '../config/constants';

export interface EmailRequest {
  recipientEmail: string;
  subject: string;
  bodyContent: string;
  ccEmails?: string[];
  bccEmails?: string[];
  isHtmlContent?: boolean;
  priority?: 'HIGH' | 'NORMAL' | 'LOW' | string;
}

export interface EmailResponse {
  wasSuccessfullySent: boolean;
  statusMessage: string;
  recipientEmail: string;
  timestampMillis: number;
}

export const emailService = {
  sendSimple: (payload: EmailRequest): Promise<EmailResponse> =>
    post(ENDPOINTS.EMAIL_SEND_SIMPLE, payload),

  sendAdvanced: (payload: EmailRequest): Promise<EmailResponse> =>
    post(ENDPOINTS.EMAIL_SEND_ADVANCED, payload),

  sendHtml: (payload: EmailRequest): Promise<EmailResponse> =>
    post(ENDPOINTS.EMAIL_SEND_HTML, payload),

  testEmail: async (recipientEmail: string): Promise<{ message?: string; success?: boolean }> => {
    const response = await apiClient.get(ENDPOINTS.EMAIL_TEST, { params: { recipientEmail } });
    return response.data;
  },

  health: async (): Promise<{ status: string; message: string; timestamp: number }> => {
    return get(ENDPOINTS.EMAIL_HEALTH);
  },
};
