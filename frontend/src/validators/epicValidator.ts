import { z } from 'zod';

// Epic validation schemas
export const epicSchema = z.object({
  name: z.string().min(1, 'Epic name is required').max(150),
  description: z.string().optional(),
  projectId: z.number().positive('Project is required'),
  managerId: z.number().positive().optional(),
  assignedToUserId: z.number().positive().optional(),
  estimatedStartDate: z.string().optional(),
  estimatedEndDate: z.string().optional(),
  status: z.enum(['PLANNING', 'IN_PROGRESS', 'COMPLETED', 'ON_HOLD']).optional(),
});

export type EpicFormData = z.infer<typeof epicSchema>;
