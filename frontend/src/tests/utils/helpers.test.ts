import { describe, it, expect } from 'vitest';
import { formatDate } from '../../utils/helpers';

describe('Helper Functions', () => {
  describe('formatDate', () => {
    it('formats date correctly', () => {
      const date = '2026-02-12';
      const formatted = formatDate(date);
      expect(formatted).toBeTruthy();
    });

    it('handles empty date', () => {
      const formatted = formatDate('');
      expect(formatted).toBe('N/A');
    });

    it('handles null date', () => {
      const formatted = formatDate(null);
      expect(formatted).toBe('N/A');
    });
  });
});
