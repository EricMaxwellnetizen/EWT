import { z } from 'zod';

// Story validation schemas
export const storySchema = z.object({
  name: z.string().min(1, 'Story name is required').max(200),
  description: z.string().optional(),
  projectId: z.number().positive('Project is required'),
  assignedToId: z.number().positive().optional(),
  epicId: z.number().positive().optional(),
  priority: z.enum(['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']).optional(),
  status: z.enum(['TODO', 'IN_PROGRESS', 'REVIEW', 'DONE']).optional(),
  estimatedHours: z.number().positive().optional(),
  dueDate: z.string().optional(),
});

export type StoryFormData = z.infer<typeof storySchema>;
