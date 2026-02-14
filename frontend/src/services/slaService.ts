import { get, post, put, del, download } from '../lib/api';
import { ENDPOINTS } from '../config/constants';
import type { SlaRule, SlaRuleFormData } from '../types';

export const slaService = {
  getAll: (): Promise<SlaRule[]> => get(ENDPOINTS.SLA_RULES),

  getById: (id: number): Promise<SlaRule> => get(ENDPOINTS.SLA_RULE_BY_ID(id)),

  create: (data: SlaRuleFormData): Promise<SlaRule> => post(ENDPOINTS.SLA_RULE_CREATE, data),

  update: (id: number, data: Partial<SlaRuleFormData>): Promise<SlaRule> => 
    put(ENDPOINTS.SLA_RULE_UPDATE(id), data),

  delete: (id: number): Promise<void> => del(ENDPOINTS.SLA_RULE_DELETE(id)),

  downloadDocument: (): Promise<void> => download(ENDPOINTS.SLA_DOWNLOAD, 'sla-rules.docx'),
};
