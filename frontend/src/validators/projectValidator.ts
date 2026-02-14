import { z } from 'zod';

// Project validation schemas
export const projectSchema = z.object({
  name: z.string().min(1, 'Project name is required').max(100),
  description: z.string().optional(),
  clientId: z.number().positive('Client is required'),
  managerId: z.number().positive('Manager is required'),
  startDate: z.string().optional(),
  endDate: z.string().optional(),
  budget: z.number().positive().optional(),
  status: z.enum(['PLANNING', 'IN_PROGRESS', 'ON_HOLD', 'COMPLETED', 'CANCELLED']).optional(),
});

export type ProjectFormData = z.infer<typeof projectSchema>;
