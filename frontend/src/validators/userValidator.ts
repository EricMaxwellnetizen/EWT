import { z } from 'zod';

// User validation schemas
export const userSchema = z.object({
  firstName: z.string().min(1, 'First name is required').max(50),
  lastName: z.string().min(1, 'Last name is required').max(50),
  email: z.string().email('Invalid email address'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
  phoneNumber: z.string().optional(),
  username: z.string().min(3, 'Username must be at least 3 characters'),
  jobTitle: z.string().optional(),
  department: z.string().optional(),
  accessLevel: z.number().min(1).max(5),
  role: z.enum(['USER', 'EMPLOYEE', 'MANAGER', 'SENIOR_MANAGER', 'ADMIN']),
});

export const userUpdateSchema = userSchema.partial().omit({ password: true });

export type UserFormData = z.infer<typeof userSchema>;
export type UserUpdateFormData = z.infer<typeof userUpdateSchema>;
