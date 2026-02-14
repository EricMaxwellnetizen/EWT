import { z } from 'zod';

// Client validation schemas
export const clientSchema = z.object({
  name: z.string().min(1, 'Client name is required').max(100),
  email: z.string().email('Invalid email address'),
  phoneNumber: z.string().optional(),
  address: z.string().optional(),
  contactPerson: z.string().optional(),
  industry: z.string().optional(),
});

export type ClientFormData = z.infer<typeof clientSchema>;
