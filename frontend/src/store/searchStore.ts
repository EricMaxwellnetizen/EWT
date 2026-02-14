import { create } from 'zustand';

interface SearchResult {
  id: string | number;
  type: 'project' | 'epic' | 'story' | 'client' | 'user' | 'sla';
  title: string;
  description?: string;
}

interface SearchState {
  query: string;
  results: SearchResult[];
  isSearching: boolean;
  setQuery: (query: string) => void;
  setResults: (results: SearchResult[]) => void;
  setIsSearching: (searching: boolean) => void;
  clearSearch: () => void;
}

export const useSearchStore = create<SearchState>((set) => ({
  query: '',
  results: [],
  isSearching: false,
  
  setQuery: (query) => set({ query }),
  setResults: (results) => set({ results }),
  setIsSearching: (searching) => set({ isSearching: searching }),
  clearSearch: () => set({ query: '', results: [], isSearching: false }),
}));
