import { describe, it, expect } from 'vitest';
import { 
  buildUserCreatePayload, 
  buildUserUpdatePayload 
} from '../../builders/userBuilder';
import type { UserInput } from '../../types';

describe('User Builder', () => {
  describe('buildUserCreatePayload', () => {
    it('trims and formats user data correctly', () => {
      const input: UserInput = {
        firstName: '  John  ',
        lastName: '  Doe  ',
        email: '  JOHN@EXAMPLE.COM  ',
        password: 'password123',
        username: '  johndoe  ',
        phoneNumber: '  123-456-7890  ',
        jobTitle: '  Developer  ',
        joiningDate: '2026-01-01',
        reportingToId: 5,
        department: '  Engineering  ',
        accessLevel: 3,
        role: 'EMPLOYEE',
        type: 'employee',
      };

      const result = buildUserCreatePayload(input);

      expect(result.firstName).toBe('John');
      expect(result.lastName).toBe('Doe');
      expect(result.email).toBe('john@example.com');
      expect(result.username).toBe('johndoe');
    });

    it('handles optional fields', () => {
      const input: UserInput = {
        firstName: 'Jane',
        lastName: 'Smith',
        email: 'jane@example.com',
        password: 'pass123',
        username: 'janesmith',
        accessLevel: 2,
        role: 'USER',
        type: 'user',
      };

      const result = buildUserCreatePayload(input);

      expect(result.phoneNumber).toBe('');
      expect(result.jobTitle).toBe('');
      expect(result.department).toBe('');
      expect(result.reportingToId).toBeUndefined();
    });
  });

  describe('buildUserUpdatePayload', () => {
    it('only includes provided fields', () => {
      const input: Partial<UserInput> = {
        firstName: 'Updated',
        email: 'updated@example.com',
      };

      const result = buildUserUpdatePayload(input);

      expect(result.firstName).toBe('Updated');
      expect(result.email).toBe('updated@example.com');
      expect(result.lastName).toBeUndefined();
      expect(result.password).toBeUndefined();
    });
  });
});
