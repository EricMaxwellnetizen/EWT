import { describe, it, expect, vi } from 'vitest';
import axios from 'axios';
import { get, post, put, del } from '../../lib/api';

// Mock axios
vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn(),
      interceptors: {
        request: { use: vi.fn(), eject: vi.fn() },
        response: { use: vi.fn(), eject: vi.fn() },
      },
    })),
  },
}));

describe('API Client', () => {
  it('exports get function', () => {
    expect(get).toBeDefined();
    expect(typeof get).toBe('function');
  });

  it('exports post function', () => {
    expect(post).toBeDefined();
    expect(typeof post).toBe('function');
  });

  it('exports put function', () => {
    expect(put).toBeDefined();
    expect(typeof put).toBe('function');
  });

  it('exports del function', () => {
    expect(del).toBeDefined();
    expect(typeof del).toBe('function');
  });
});
