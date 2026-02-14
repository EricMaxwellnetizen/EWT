import React, { useState, useRef, useEffect, useCallback } from 'react';
import { Search, Loader2, Sun, Moon } from 'lucide-react';
import { useAuthStore } from '../../store/authStore';
import { useThemeStore } from '../../store/themeStore';
import { useNavigate } from 'react-router-dom';
import { NotificationBell } from '../ui/NotificationBell';
import { projectService } from '../../services/projectService';
import { epicService } from '../../services/epicService';
import { storyService } from '../../services/storyService';
import { clientService } from '../../services/clientService';
import { userService } from '../../services/userService';
import { slaService } from '../../services/slaService';
import toast from 'react-hot-toast';

// Utility function to debounce
const debounce = <T extends (...args: any[]) => any>(func: T, wait: number) => {
  let timeout: ReturnType<typeof setTimeout>;
  return ((...args: Parameters<T>) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  }) as T;
};

interface SearchResult {
  id: string | number;
  type: 'project' | 'epic' | 'story' | 'client' | 'user' | 'sla';
  title: string;
  description?: string;
  allowed?: boolean;
}

const searchAllEntities = async (query: string): Promise<SearchResult[]> => {
  if (!query.trim()) return [];

  const lowerQuery = query.toLowerCase();
  const results: SearchResult[] = [];

  try {
    // Search projects
    const projects = await projectService.getAll();
    projects
      ?.filter(p =>
        p.name.toLowerCase().includes(lowerQuery) ||
        (p.description?.toLowerCase().includes(lowerQuery) ?? false)
      )
      .slice(0, 5)
      .forEach(p => {
        results.push({
          id: p.id!,
          type: 'project',
          title: p.name,
          description: p.description,
        });
      });

    // Search epics
    const epics = await epicService.getAll();
    epics
      ?.filter(e => e.name.toLowerCase().includes(lowerQuery))
      .slice(0, 5)
      .forEach(e => {
        results.push({
          id: e.id!,
          type: 'epic',
          title: e.name,
          description: e.description,
        });
      });

    // Search stories
    const stories = await storyService.getAll();
    stories
      ?.filter(s => s.name.toLowerCase().includes(lowerQuery))
      .slice(0, 5)
      .forEach(s => {
        results.push({
          id: s.id!,
          type: 'story',
          title: s.name,
          description: s.description,
        });
      });

    // Search clients
    const clients = await clientService.getAll();
    clients
      ?.filter(c => c.name.toLowerCase().includes(lowerQuery) || c.email.toLowerCase().includes(lowerQuery))
      .slice(0, 5)
      .forEach(c => {
        results.push({
          id: c.id!,
          type: 'client',
          title: c.name,
          description: c.email,
        });
      });

    // Search users
    const users = await userService.getAll();
    users
      ?.filter(u =>
        `${u.firstName} ${u.lastName}`.toLowerCase().includes(lowerQuery) ||
        u.email.toLowerCase().includes(lowerQuery)
      )
      .slice(0, 5)
      .forEach(u => {
        results.push({
          id: u.id!,
          type: 'user',
          title: `${u.firstName} ${u.lastName}`,
          description: u.email,
        });
      });

    // Search SLA rules
    const slaRules = await slaService.getAll();
    slaRules
      ?.filter(r => r.name.toLowerCase().includes(lowerQuery))
      .slice(0, 5)
      .forEach(r => {
        results.push({
          id: r.id!,
          type: 'sla',
          title: r.name,
          description: r.description,
        });
      });
  } catch (error) {
    console.error('Search error:', error);
  }

  return results.slice(0, 10); // Limit to 10 total results
};

export const Header: React.FC = () => {
  const { user, hasAccessLevel } = useAuthStore();
  const { theme, toggleTheme } = useThemeStore();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [showResults, setShowResults] = useState(false);
  const searchRef = useRef<HTMLDivElement>(null);
  const [results, setResults] = useState<SearchResult[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const debouncedSearchRef = useRef<ReturnType<typeof debounce> | null>(null);

  const handleProfileClick = () => {
    navigate('/profile');
  };

  const performSearch = useCallback(async (query: string) => {
    if (!query.trim()) {
      setResults([]);
      setShowResults(false);
      return;
    }

    setIsSearching(true);
    setShowResults(true);
    const searchResults = await searchAllEntities(query);
    setResults(searchResults);
    setIsSearching(false);
  }, []);

  // Initialize debounced search function with 500ms delay
  useEffect(() => {
    debouncedSearchRef.current = debounce(performSearch, 500);
  }, [performSearch]);

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    if (debouncedSearchRef.current) {
      debouncedSearchRef.current(query);
    }
  };

  const handleResultClick = (result: SearchResult) => {
    if (result.allowed === false) {
      toast.error(`You don't have access to view ${result.type}s`);
      return;
    }

    switch (result.type) {
      case 'project':
        navigate(`/projects/${result.id}`);
        break;
      case 'epic':
        navigate(`/epics/${result.id}`);
        break;
      case 'story':
        navigate(`/stories/${result.id}`);
        break;
      case 'client':
        navigate(`/clients/${result.id}`);
        break;
      case 'user':
        navigate(`/users/${result.id}`);
        break;
      case 'sla':
        navigate(`/sla/${result.id}`);
        break;
    }
    setSearchQuery('');
    setResults([]);
    setShowResults(false);
  };

  // Close results when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (searchRef.current && !searchRef.current.contains(event.target as Node)) {
        setShowResults(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const accessByType: Record<SearchResult['type'], number> = {
    project: 1,
    epic: 2,
    story: 2,
    client: 1,
    user: 1,
    sla: 3,
  };

  const visibleResults = results.map((result) => ({
    ...result,
    allowed: hasAccessLevel(accessByType[result.type]),
  }));

  return (
    <header className="h-16 glass-card flex items-center justify-between px-6">
      {/* Search */}
      <div className="flex-1 max-w-2xl" ref={searchRef}>
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400 dark:text-gray-500" />
          <input
            type="text"
            placeholder="Search projects, epics, stories, clients, users, SLA rules..."
            value={searchQuery}
            onChange={(e) => handleSearch(e.target.value)}
            onFocus={() => searchQuery && setShowResults(true)}
            className="w-full pl-10 pr-4 py-2 bg-white/80 dark:bg-gray-700/80 border border-slate-200 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500 dark:text-gray-100 dark:placeholder-gray-400"
          />

          {/* Search Results Dropdown */}
          {showResults && (
            <div className="absolute top-full left-0 right-0 mt-2 bg-white dark:bg-gray-800 border border-slate-200 dark:border-gray-700 rounded-lg shadow-xl z-50 max-h-96 overflow-y-auto">
              {isSearching ? (
                <div className="flex items-center justify-center p-8">
                  <Loader2 className="w-5 h-5 text-slate-400 animate-spin" />
                </div>
              ) : visibleResults.length === 0 ? (
                <div className="p-4 text-center text-slate-500 dark:text-gray-400">
                  {searchQuery ? 'No results found' : 'Start typing to search'}
                </div>
              ) : (
                <div className="divide-y divide-slate-200 dark:divide-gray-700">
                  {visibleResults.map((result, idx) => (
                    <button
                      key={`${result.type}-${result.id}-${idx}`}
                      onClick={() => handleResultClick(result)}
                      className={`w-full text-left px-4 py-3 transition-colors flex items-start justify-between group ${
                        result.allowed === false
                          ? 'opacity-50 cursor-not-allowed'
                          : 'hover:bg-slate-50 dark:hover:bg-gray-700'
                      }`}
                      disabled={result.allowed === false}
                    >
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                          <h4 className="font-medium text-slate-900 dark:text-gray-100 truncate">
                            {result.title}
                          </h4>
                          <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-slate-100 dark:bg-gray-700 text-slate-600 dark:text-gray-300 flex-shrink-0">
                            {result.type}
                          </span>
                          {result.allowed === false && (
                            <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-red-50 text-red-600 flex-shrink-0">
                              No access
                            </span>
                          )}
                        </div>
                        {result.description && (
                          <p className="text-xs text-slate-500 dark:text-gray-400 truncate mt-1">
                            {result.description}
                          </p>
                        )}
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Right Actions */}
      <div className="flex items-center gap-4">
        {/* Theme Toggle */}
        <button
          onClick={toggleTheme}
          className="p-2 text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg transition-colors"
          title={theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode'}
        >
          {theme === 'dark' ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
        </button>

        {/* Real Notifications Bell */}
        <NotificationBell />
        
        <button 
          onClick={handleProfileClick}
          className="flex items-center gap-3 pl-4 border-l border-slate-200 dark:border-gray-700 hover:opacity-75 transition-opacity"
        >
          <div className="text-right">
            <p className="text-sm font-medium text-slate-900 dark:text-gray-100">{user?.username || user?.email || 'User'}</p>
            <p className="text-xs text-slate-500 dark:text-gray-400">{user?.email || 'No email'}</p>
          </div>
          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-primary-500 to-secondary-500 flex items-center justify-center text-white font-semibold cursor-pointer">
            {(user?.username || user?.email || 'U')?.charAt(0).toUpperCase()}
          </div>
        </button>
      </div>
    </header>
  );
};
