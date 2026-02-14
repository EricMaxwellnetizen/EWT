# Frontend Technical Documentation
## Enterprise Workflow Task Management System

**Version:** 1.0.0  
**Framework:** React 19 + TypeScript  
**UI Library:** shadcn/ui + Tailwind CSS  
**Last Updated:** February 12, 2026

---

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [Project Structure](#project-structure)
5. [State Management](#state-management)
6. [Routing & Navigation](#routing--navigation)
7. [Authentication Flow](#authentication-flow)
8. [API Integration](#api-integration)
9. [Core Features](#core-features)
10. [Undo/Redo System](#undoredo-system)
11. [Component Library](#component-library)
12. [Styling & Theming](#styling--theming)
13. [Type System](#type-system)
14. [Build & Deployment](#build--deployment)

---

## System Overview

The Enterprise Workflow Task Management System frontend is a modern React application providing a comprehensive interface for managing projects, epics, stories, users, clients, and SLA rules.

### Key Features
- **Hierarchical Task Management**: Visual project → epic → story hierarchy
- **Role-Based UI**: Dynamic components based on user access level
- **Dark Mode**: System-wide theme toggle with persistence
- **Undo/Redo**: Global undo/redo for all create/update/delete operations
- **Real-Time Notifications**: Toast notifications with undo capability
- **Analytics Dashboard**: Charts and metrics
- **File Management**: Upload and download capabilities
- **Responsive Design**: Mobile-first approach with Tailwind CSS

---

## Architecture

### **Component Architecture**

```
┌─────────────────────────────────────────┐
│           App.tsx (Root)                │
├─────────────────────────────────────────┤
│    AuthProvider + ErrorBoundary         │
├─────────────────────────────────────────┤
│         Router (React Router)           │
├─────────────────────────────────────────┤
│      Layout Components                  │
│   (Header, Sidebar, Footer)             │
├─────────────────────────────────────────┤
│         Page Components                 │
│  (Dashboard, Projects, Users, etc.)     │
├─────────────────────────────────────────┤
│       Service Layer (API calls)         │
├─────────────────────────────────────────┤
│      State Management (Zustand)         │
└─────────────────────────────────────────┘
```

### **Data Flow**

1. **User Action** → Component Event Handler
2. **Event Handler** → Service Layer Function
3. **Service Layer** → API Call (`api.ts`)
4. **API Response** → State Update (Zustand store)
5. **State Update** → Component Re-render
6. **Undo/Redo** → Stored in `undoRedoStore`

---

## Technology Stack

| Category | Technology | Version |
|----------|------------|---------|
| **Framework** | React | 19.0.0 |
| **Language** | TypeScript | 5.6.2 |
| **Build Tool** | Vite | 6.2.0 |
| **Router** | React Router | 7.5.1 |
| **State Management** | Zustand | 5.0.4 |
| **HTTP Client** | Axios | 1.7.9 |
| **UI Framework** | shadcn/ui | Latest |
| **CSS Framework** | Tailwind CSS | 3.4.17 |
| **Icons** | Lucide React | 0.469.0 |
| **Charts** | Recharts | 2.14.1 |
| **Forms** | React Hook Form | 7.54.2 |
| **Validation** | Zod | 3.24.1 |
| **Date Handling** | date-fns | 4.1.0 |
| **Notifications** | React Hot Toast | 2.4.1 |

---

## Project Structure

```
frontend/
├── public/
│   └── vite.svg
├── src/
│   ├── main.tsx                    # Entry point
│   ├── App.tsx                     # Root component with routing
│   ├── index.css                   # Global styles
│   ├── assets/                     # Static assets
│   ├── components/
│   │   ├── ErrorBoundary.tsx      # Global error handler
│   │   ├── auth/
│   │   │   ├── AuthProvider.tsx   # Auth context
│   │   │   └── ProtectedRoute.tsx # Route guard
│   │   ├── layout/
│   │   │   ├── Header.tsx         # Top navigation
│   │   │   ├── Sidebar.tsx        # Side navigation
│   │   │   └── Footer.tsx         # Bottom bar
│   │   └── ui/                    # shadcn/ui components
│   │       ├── button.tsx
│   │       ├── card.tsx
│   │       ├── dialog.tsx
│   │       ├── input.tsx
│   │       └── ... (40+ components)
│   ├── config/
│   │   └── constants.ts           # App-wide constants
│   ├── hooks/
│   │   ├── useUndoRedo.ts         # Undo/redo hook
│   │   └── useWebSocket.ts        # WebSocket (if implemented)
│   ├── lib/
│   │   └── api.ts                 # Axios HTTP client
│   ├── pages/
│   │   ├── LandingPage.tsx        # Public landing page
│   │   ├── auth/
│   │   │   ├── LoginPage.tsx      # Login form
│   │   │   └── RegisterPage.tsx   # Registration form
│   │   ├── dashboard/
│   │   │   └── DashboardPage.tsx  # Main dashboard
│   │   ├── projects/
│   │   │   ├── ProjectsPage.tsx   # Project list
│   │   │   └── ProjectDetailPage.tsx
│   │   ├── epics/
│   │   │   └── EpicsPage.tsx
│   │   ├── stories/
│   │   │   └── StoriesPage.tsx
│   │   ├── users/
│   │   │   ├── UsersPage.tsx      # User management
│   │   │   └── UserProfilePage.tsx
│   │   ├── clients/
│   │   │   └── ClientsPage.tsx
│   │   ├── sla/
│   │   │   └── SLAPage.tsx
│   │   ├── reports/
│   │   │   └── ReportsPage.tsx
│   │   └── notifications/
│   │       └── NotificationsPage.tsx
│   ├── services/
│   │   ├── authService.ts         # Authentication
│   │   ├── userService.ts         # User CRUD
│   │   ├── projectService.ts      # Project CRUD
│   │   ├── epicService.ts         # Epic CRUD
│   │   ├── storyService.ts        # Story CRUD
│   │   ├── clientService.ts       # Client CRUD
│   │   ├── slaService.ts          # SLA CRUD
│   │   ├── notificationService.ts # Notifications
│   │   ├── analyticsService.ts    # Analytics
│   │   ├── reportService.ts       # Reports
│   │   ├── timeLogService.ts      # Time tracking
│   │   ├── activityService.ts     # Activity logs
│   │   ├── fileService.ts         # File upload/download
│   │   └── emailService.ts        # Email operations
│   ├── store/
│   │   ├── authStore.ts           # Auth state
│   │   ├── uiStore.ts             # UI state (sidebar, modals)
│   │   ├── themeStore.ts          # Dark mode state
│   │   ├── undoRedoStore.ts       # Undo/redo state
│   │   └── searchStore.ts         # Search state
│   ├── types/
│   │   └── index.ts               # TypeScript interfaces
│   └── utils/
│       ├── helpers.ts             # Utility functions
│       ├── customToast.tsx        # Toast notifications
│       └── undoToast.tsx          # Undo toast component
├── eslint.config.js
├── tailwind.config.js
├── tsconfig.json
├── vite.config.ts
└── package.json
```

---

## State Management

### **Zustand Stores**

Zustand is a lightweight state management solution using React hooks.

#### **authStore.ts**

Manages authentication state:

```typescript
interface AuthStore {
  user: User | null;              // Current logged-in user
  token: string | null;           // JWT token
  isAuthenticated: boolean;       // Auth status
  accessLevel: number;            // User access level (1-5)
  login: (credentials) => void;   // Login action
  logout: () => void;             // Logout action
  setUser: (user) => void;        // Update user
}
```

**Persistence:** Token stored in `localStorage` as `authToken`

#### **themeStore.ts**

Manages dark mode:

```typescript
interface ThemeStore {
  isDarkMode: boolean;            // Theme state
  toggleTheme: () => void;        // Toggle action
}
```

**Persistence:** Stored in `localStorage` as `theme`  
**CSS:** Toggles `dark` class on `<html>` element

#### **undoRedoStore.ts**

Manages undo/redo operations:

```typescript
interface UndoRedoStore {
  history: UndoableAction[];      // Past actions
  currentIndex: number;           // Current position
  canUndo: boolean;               // Undo available
  canRedo: boolean;               // Redo available
  addAction: (action) => void;    // Add new action
  undo: () => void;               // Undo last action
  redo: () => void;               // Redo action
  clear: () => void;              // Clear history
}
```

**Max History:** 50 actions

#### **uiStore.ts**

Manages UI state:

```typescript
interface UIStore {
  isSidebarOpen: boolean;         // Sidebar visibility
  isModalOpen: boolean;           // Modal state
  modalData: any;                 // Modal props
  toggleSidebar: () => void;
  openModal: (data) => void;
  closeModal: () => void;
}
```

#### **searchStore.ts**

Manages global search:

```typescript
interface SearchStore {
  query: string;                  // Search query
  results: any[];                 // Search results
  isSearching: boolean;           // Loading state
  setQuery: (q) => void;
  search: () => void;
  clearResults: () => void;
}
```

---

## Routing & Navigation

### **Router Configuration** (App.tsx)

```tsx
<BrowserRouter>
  <Routes>
    {/* Public Routes */}
    <Route path="/" element={<LandingPage />} />
    <Route path="/login" element={<LoginPage />} />
    <Route path="/register" element={<RegisterPage />} />

    {/* Protected Routes */}
    <Route element={<ProtectedRoute />}>
      <Route path="/dashboard" element={<DashboardPage />} />
      <Route path="/projects" element={<ProjectsPage />} />
      <Route path="/projects/:id" element={<ProjectDetailPage />} />
      <Route path="/epics" element={<EpicsPage />} />
      <Route path="/stories" element={<StoriesPage />} />
      <Route path="/users" element={<UsersPage />} />
      <Route path="/clients" element={<ClientsPage />} />
      <Route path="/sla" element={<SLAPage />} />
      <Route path="/reports" element={<ReportsPage />} />
      <Route path="/notifications" element={<NotificationsPage />} />
      <Route path="/profile" element={<UserProfilePage />} />
    </Route>

    {/* 404 */}
    <Route path="*" element={<NotFoundPage />} />
  </Routes>
</BrowserRouter>
```

### **ProtectedRoute Component**

Redirects to `/login` if not authenticated:

```tsx
const ProtectedRoute = () => {
  const { isAuthenticated } = useAuthStore();
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" />;
};
```

### **Access Level-Based Rendering**

Components check `accessLevel` from `authStore`:

```tsx
{accessLevel >= 4 && (
  <Button onClick={createProject}>Create Project</Button>
)}
```

**Access Levels:**
- 1 = USER (View only)
- 2 = EMPLOYEE (Create stories, view assigned tasks)
- 3 = LEAD (Manage epics, approve stories)
- 4 = MANAGER (Manage projects, users)
- 5 = ADMIN (Full access, audit logs)

---

## Authentication Flow

### **Login Process**

1. User submits credentials at `/login`
2. `authService.login()` calls `POST /api/v1/auth/login`
3. Backend returns JWT token + user object
4. Frontend stores token in `localStorage`
5. Frontend updates `authStore` with user and token
6. Redirect to `/dashboard`

**Code Flow:**
```
LoginPage.tsx → authService.login() → api.post() → authStore.login() → navigate('/dashboard')
```

### **Token Validation**

Every API request includes:
```
Authorization: Bearer <JWT_TOKEN>
```

**Automatic Handling:**
- Valid token → Request proceeds
- Expired token → 401 error → Logout → Redirect to `/login`
- No token → Redirect to `/login`

### **Logout Process**

1. User clicks logout button
2. `authService.logout()` calls `POST /api/v1/auth/logout`
3. Frontend clears `localStorage` (`authToken`)
4. Frontend resets `authStore`
5. Redirect to `/login`

### **OAuth2 Google Login**

1. User clicks "Sign in with Google"
2. Redirects to `http://localhost:8082/oauth2/authorization/google`
3. Google OAuth flow
4. Backend creates/updates user
5. Redirects back with token
6. Frontend stores token and user

---

## API Integration

### **HTTP Client** (lib/api.ts)

Centralized Axios instance with:
- Automatic JWT token injection
- Request/response logging
- Case conversion (snake_case ↔ camelCase)
- Error handling with toast notifications

**Key Functions:**

```typescript
// Standard CRUD operations
get(endpoint: string): Promise<any>
post(endpoint: string, data: any): Promise<any>
put(endpoint: string, data: any): Promise<any>
del(endpoint: string): Promise<any>
patch(endpoint: string, data: any): Promise<any>

// Root-level requests (no /api/v1 prefix)
getRoot(endpoint: string): Promise<any>
postRoot(endpoint: string, data: any): Promise<any>
putRoot(endpoint: string, data: any): Promise<any>
delRoot(endpoint: string): Promise<any>

// File download
download(endpoint: string, filename: string): Promise<void>
```

**Interceptors:**

**Request Interceptor:**
1. Convert request body keys to snake_case
2. Add `Authorization` header with JWT token
3. Log request details

**Response Interceptor:**
1. Convert response data keys to camelCase
2. Handle 401 errors → Logout
3. Handle other errors → Toast notification
4. Log response details

### **Service Layer**

Each service file provides CRUD operations for a specific entity:

#### **userService.ts**
- `getAllUsers()` → `GET /api/v1/user/get`
- `getUserById(id)` → `GET /api/v1/user/{id}`
- `createUser(data)` → `POST /api/v1/user/create`
- `updateUser(id, data)` → `PUT /api/v1/user/{id}`
- `deleteUser(id)` → `DELETE /api/v1/user/{id}`
- `getEditableUsers()` → `GET /api/v1/user/editable`
- `canEditUser(id)` → `GET /api/v1/user/can-edit/{id}`
- `changePassword(oldPwd, newPwd)` → `PUT /api/v1/user/me/password`
- `uploadProfilePicture(file)` → `POST /api/v1/user/me/upload-picture`

**Data Normalization:**
- Converts `first_name`, `last_name` to `fullName`
- Handles `reportingTo` object nesting

#### **projectService.ts**
- `getAllProjects()` → `GET /api/v1/project/get`
- `getPaginatedProjects(page, size)` → `GET /api/v1/project/paginated`
- `getProjectById(id)` → `GET /api/v1/project/{id}`
- `createProject(data)` → `POST /api/v1/project/create`
- `updateProject(id, data)` → `PUT /api/v1/project/{id}`
- `deleteProject(id)` → `DELETE /api/v1/project/{id}`
- `downloadProjectsDocument(managerId)` → `GET /api/v1/project/manager/{managerId}/download`

**Data Normalization:**
- Converts `client` object to `clientName`
- Converts `manager` object to `managerName`

#### **analyticsService.ts**
- `getDashboardMetrics()` → `GET /api/v1/analytics/dashboard`
- `getProjectAnalytics(id)` → `GET /api/v1/analytics/project/{id}`
- `getTeamAnalytics()` → `GET /api/v1/analytics/team`
- `getWorkloadDistribution()` → `GET /api/v1/analytics/workload`
- `getRiskAnalysis(projectId)` → `GET /api/v1/analytics/project/{id}/risks`

All services follow the same pattern with proper error handling and data transformation.

---

## Core Features

### **1. Dashboard**

**File:** `pages/dashboard/DashboardPage.tsx`

**Components:**
- **Metrics Cards**: Total projects, active stories, team members, completion rate
- **Recent Activity Feed**: Latest user actions
- **Charts**: Story status distribution, project timeline, workload by user
- **Quick Actions**: Create project, create story, view reports

**Data Sources:**
- `analyticsService.getDashboardMetrics()`
- `activityService.getRecentActivity()`

### **2. Project Management**

**File:** `pages/projects/ProjectsPage.tsx`

**Features:**
- **Project List**: Table with name, client, manager, dates, status
- **Filters**: By client, manager, status, date range
- **Create Project**: Modal with form validation
- **Edit Project**: Inline editing or modal
- **Delete Project**: Confirmation dialog with cascade warning
- **Hierarchy View**: Expandable tree showing epics and stories

**Access Control:**
- Level 4+: Create, edit, delete projects
- Level 3: View all projects, edit assigned projects
- Level 1-2: View only

### **3. Epic Management**

**File:** `pages/epics/EpicsPage.tsx`

**Features:**
- **Epic Cards**: Visual cards with progress bars
- **Story List**: Show stories within each epic
- **Approval Workflow**: Approve/reject epic completion
- **Progress Tracking**: % complete based on stories

**Access Control:**
- Level 3+: Create, edit, approve epics
- Level 2: View assigned epics

### **4. Story Management**

**File:** `pages/stories/StoriesPage.tsx`

**Features:**
- **Kanban Board**: Drag-and-drop between TODO, IN_PROGRESS, REVIEW, DONE
- **Story Cards**: Title, description, assignee, priority, due date
- **Time Tracking**: Log actual hours vs estimated
- **Comments**: Threaded comments on stories
- **Status Transitions**: Automatic epic/project completion on all stories done

**Access Control:**
- Level 2+: Create, edit, complete assigned stories
- Level 3+: Edit any story, approve completions

### **5. User Management**

**File:** `pages/users/UsersPage.tsx`

**Features:**
- **User Table**: Name, email, role, department, access level
- **Create User**: Form with access level dropdown (filtered by current user level)
- **Edit User**: Only editable users (lower access level)
- **Delete User**: Confirmation with cascade warning
- **Reporting Hierarchy**: Tree view of manager → employee relationships

**Access Control:**
- Level 5: Manage all users
- Level 4: Manage levels 1-3
- Level 3: Manage levels 1-2

**Editable Users Logic:**
```typescript
const canEdit = currentUserAccessLevel > targetUserAccessLevel;
```

### **6. Client Management**

**File:** `pages/clients/ClientsPage.tsx`

**Features:**
- **Client Cards**: Name, email, phone, address
- **Create Client**: Modal form
- **Edit Client**: Inline or modal
- **Delete Client**: Check for associated projects
- **Hierarchy View**: Show all projects for client

**Access Control:**
- Level 4+: Full CRUD
- Level 1-3: View only

### **7. SLA Rules**

**File:** `pages/sla/SLAPage.tsx`

**Features:**
- **SLA Table**: Name, priority, response time, resolution time, status
- **Create SLA**: Form with time inputs
- **Edit SLA**: Modal form
- **Activate/Deactivate**: Toggle active status

**Access Control:**
- Level 4+: Create, edit, activate
- Level 5: Delete

### **8. Notifications**

**File:** `pages/notifications/NotificationsPage.tsx`

**Features:**
- **Notification List**: Time, message, type (success/warning/error/info)
- **Unread Badge**: Count in header
- **Mark as Read**: Click notification
- **Clear All**: Button to delete all notifications
- **Auto-Refresh**: Poll every 30 seconds

**Real-Time Updates:**
```typescript
useEffect(() => {
  const interval = setInterval(() => {
    notificationService.getUnreadCount();
  }, 30000); // 30 seconds
  return () => clearInterval(interval);
}, []);
```

### **9. Reports**

**File:** `pages/reports/ReportsPage.tsx`

**Features:**
- **Report Types**: Project summary, user activity, time logs, SLA compliance
- **Filters**: Date range, user, project, client
- **Export**: PDF, Excel, CSV
- **Charts**: Bar, line, pie charts using Recharts

**Data Sources:**
- `reportService.getProjectSummary()`
- `reportService.getUserActivity()`
- `reportService.getSLACompliance()`

---

## Undo/Redo System

### **Architecture**

The undo/redo system records all create, update, and delete operations and allows reverting them.

**Components:**
1. **undoRedoStore.ts** - Zustand store for history
2. **useUndoRedo.ts** - React hook for actions
3. **undoToast.tsx** - Toast with undo button
4. **Service layer integration** - Auto-record actions

### **UndoableAction Interface**

```typescript
interface UndoableAction {
  id: string;                     // Unique action ID
  type: 'CREATE' | 'UPDATE' | 'DELETE';
  entityType: string;             // 'User', 'Project', etc.
  entityId: number;               // Entity ID
  data: any;                      // Entity data snapshot
  previousData?: any;             // For UPDATE: old data
  timestamp: number;              // When action occurred
  description: string;            // Human-readable description
  undoFn: () => Promise<void>;    // Function to undo
  redoFn: () => Promise<void>;    // Function to redo
}
```

### **Recording Actions**

**Example: Create Project**

```typescript
const createProject = async (projectData) => {
  const createdProject = await projectService.createProject(projectData);
  
  // Record undoable action
  addUndoAction({
    type: 'CREATE',
    entityType: 'Project',
    entityId: createdProject.id,
    data: createdProject,
    description: `Created project "${createdProject.name}"`,
    undoFn: async () => {
      await projectService.deleteProject(createdProject.id);
      customToast.success(`Undid: Created project "${createdProject.name}"`);
    },
    redoFn: async () => {
      await projectService.createProject(projectData);
      customToast.success(`Redid: Created project "${createdProject.name}"`);
    }
  });
  
  customToast.successWithUndo(
    `Created project "${createdProject.name}"`,
    () => undo()
  );
};
```

**Example: Update User**

```typescript
const updateUser = async (userId, newData) => {
  const oldUser = await userService.getUserById(userId);
  const updatedUser = await userService.updateUser(userId, newData);
  
  addUndoAction({
    type: 'UPDATE',
    entityType: 'User',
    entityId: userId,
    data: updatedUser,
    previousData: oldUser,
    description: `Updated user "${updatedUser.fullName}"`,
    undoFn: async () => {
      await userService.updateUser(userId, oldUser);
      customToast.success(`Undid: Updated user "${updatedUser.fullName}"`);
    },
    redoFn: async () => {
      await userService.updateUser(userId, newData);
      customToast.success(`Redid: Updated user "${updatedUser.fullName}"`);
    }
  });
  
  customToast.successWithUndo(
    `Updated user "${updatedUser.fullName}"`,
    () => undo()
  );
};
```

**Example: Delete Story**

```typescript
const deleteStory = async (storyId) => {
  const storyToDelete = await storyService.getStoryById(storyId);
  await storyService.deleteStory(storyId);
  
  addUndoAction({
    type: 'DELETE',
    entityType: 'Story',
    entityId: storyId,
    data: storyToDelete,
    description: `Deleted story "${storyToDelete.name}"`,
    undoFn: async () => {
      await storyService.createStory(storyToDelete);
      customToast.success(`Undid: Deleted story "${storyToDelete.name}"`);
    },
    redoFn: async () => {
      await storyService.deleteStory(storyId);
      customToast.success(`Redid: Deleted story "${storyToDelete.name}"`);
    }
  });
  
  customToast.successWithUndo(
    `Deleted story "${storyToDelete.name}"`,
    () => undo()
  );
};
```

### **Using the Hook**

```typescript
import { useUndoRedo } from '@/hooks/useUndoRedo';

const MyComponent = () => {
  const { undo, redo, canUndo, canRedo, addUndoAction } = useUndoRedo();

  return (
    <div>
      <Button onClick={undo} disabled={!canUndo}>Undo</Button>
      <Button onClick={redo} disabled={!canRedo}>Redo</Button>
    </div>
  );
};
```

### **Keyboard Shortcuts**

- **Ctrl+Z** (Windows) / **Cmd+Z** (Mac): Undo
- **Ctrl+Y** (Windows) / **Cmd+Shift+Z** (Mac): Redo

**Implementation:**
```typescript
useEffect(() => {
  const handleKeyDown = (e: KeyboardEvent) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'z' && !e.shiftKey) {
      e.preventDefault();
      if (canUndo) undo();
    }
    if ((e.ctrlKey || e.metaKey) && (e.key === 'y' || (e.key === 'z' && e.shiftKey))) {
      e.preventDefault();
      if (canRedo) redo();
    }
  };
  window.addEventListener('keydown', handleKeyDown);
  return () => window.removeEventListener('keydown', handleKeyDown);
}, [canUndo, canRedo, undo, redo]);
```

### **History Limits**

- **Max Actions:** 50 (configurable in `undoRedoStore.ts`)
- **Persistence:** Not persisted (clears on page refresh)
- **Performance:** Actions are shallow copies (not deep clones)

---

## Component Library

### **shadcn/ui Components**

The app uses 40+ pre-built components from shadcn/ui:

**Form Components:**
- `<Input />` - Text input with validation
- `<Button />` - Multiple variants (default, destructive, outline, ghost)
- `<Select />` - Dropdown with search
- `<Textarea />` - Multi-line input
- `<Checkbox />` - Checkbox with label
- `<RadioGroup />` - Radio buttons
- `<Switch />` - Toggle switch
- `<DatePicker />` - Calendar date picker

**Layout Components:**
- `<Card />` - Container with header and footer
- `<Dialog />` - Modal dialog
- `<Sheet />` - Slide-in panel
- `<Tabs />` - Tab navigation
- `<Accordion />` - Collapsible sections
- `<Table />` - Data table with sorting

**Feedback Components:**
- `<Alert />` - Info, warning, error, success alerts
- `<Badge />` - Status badges
- `<Progress />` - Progress bar
- `<Skeleton />` - Loading placeholder
- `<Spinner />` - Loading spinner
- `<Toast />` - Notification toast (via react-hot-toast)

**Navigation Components:**
- `<DropdownMenu />` - Dropdown with actions
- `<NavigationMenu />` - Top navigation
- `<Breadcrumb />` - Breadcrumb trail
- `<Pagination />` - Page navigation

### **Custom Components**

**ErrorBoundary.tsx**
- Catches React errors
- Shows fallback UI
- Logs errors to console

**Header.tsx**
- App logo and title
- Navigation links (conditional based on access level)
- User dropdown (profile, settings, logout)
- Notification bell with unread badge
- Theme toggle (light/dark)

**Sidebar.tsx**
- Collapsible side navigation
- Grouped menu items (Dashboard, Manage, Reports, Admin)
- Active route highlighting
- Access level-based menu filtering

**Footer.tsx**
- Copyright notice
- Quick links
- Version number

---

## Styling & Theming

### **Tailwind CSS**

Configuration in `tailwind.config.js`:

```javascript
export default {
  darkMode: 'class',  // Dark mode via .dark class
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {...},    // Brand colors
        secondary: {...},
        accent: {...},
        border: {...},     // Dark mode variants
        background: {...},
        foreground: {...},
      },
      borderRadius: {
        lg: 'var(--radius)',
        md: 'calc(var(--radius) - 2px)',
        sm: 'calc(var(--radius) - 4px)',
      },
    },
  },
  plugins: [require('tailwindcss-animate')],
};
```

### **Dark Mode**

**Toggle:**
```typescript
const { isDarkMode, toggleTheme } = useThemeStore();
```

**CSS Classes:**
```css
/* Light mode */
.bg-background { background: white; }

/* Dark mode */
.dark .bg-background { background: #0a0a0a; }
```

**Implementation:**
```typescript
useEffect(() => {
  if (isDarkMode) {
    document.documentElement.classList.add('dark');
  } else {
    document.documentElement.classList.remove('dark');
  }
}, [isDarkMode]);
```

### **Global Styles** (index.css)

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

@layer base {
  :root {
    --background: 0 0% 100%;
    --foreground: 222.2 84% 4.9%;
    --primary: 221.2 83.2% 53.3%;
    /* ... more CSS variables */
  }

  .dark {
    --background: 222.2 84% 4.9%;
    --foreground: 210 40% 98%;
    --primary: 217.2 91.2% 59.8%;
    /* ... dark mode overrides */
  }
}
```

---

## Type System

### **Core Interfaces** (types/index.ts)

```typescript
// User
export interface User {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  fullName: string;  // Computed from firstName + lastName
  email: string;
  phoneNumber?: string;
  accessLevel: number;  // 1-5
  role: string;
  department?: string;
  jobTitle?: string;
  joiningDate?: string;
  reportingTo?: User;
  reportingToId?: number;
  createdAt?: string;
  updatedAt?: string;
}

// Project
export interface Project {
  id: number;
  name: string;
  description?: string;
  client?: Client;
  clientId?: number;
  clientName?: string;  // For display
  manager?: User;
  managerId?: number;
  managerName?: string;  // For display
  startDate?: string;
  deadline?: string;
  deliverables?: string;
  isCompleted: boolean;
  epics?: Epic[];
  createdAt?: string;
  updatedAt?: string;
}

// Epic
export interface Epic {
  id: number;
  name: string;
  description?: string;
  project?: Project;
  projectId: number;
  manager?: User;
  managerId?: number;
  startDate?: string;
  dueDate?: string;
  isApproved: boolean;
  isCompleted: boolean;
  stories?: Story[];
  createdAt?: string;
  updatedAt?: string;
}

// Story
export interface Story {
  id: number;
  name: string;
  description?: string;
  epic?: Epic;
  epicId: number;
  assignedTo?: User;
  assignedToId?: number;
  status: 'TODO' | 'IN_PROGRESS' | 'REVIEW' | 'DONE';
  priority?: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  dueDate?: string;
  estimatedHours?: number;
  actualHours?: number;
  isApproved: boolean;
  createdAt?: string;
  updatedAt?: string;
  completedAt?: string;
}

// Client
export interface Client {
  id: number;
  name: string;
  email?: string;
  phoneNumber?: string;
  address?: string;
  projects?: Project[];
  createdAt?: string;
  updatedAt?: string;
}

// SLA Rule
export interface SLARule {
  id: number;
  name: string;
  description?: string;
  priorityLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  responseTimeHours: number;
  resolutionTimeHours: number;
  escalationTimeHours?: number;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// Notification
export interface Notification {
  id: number;
  userId: number;
  message: string;
  type: 'INFO' | 'WARNING' | 'ERROR' | 'SUCCESS';
  isRead: boolean;
  createdAt: string;
  readAt?: string;
}

// Pagination
export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
```

---

## Build & Deployment

### **Development**

```bash
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173`

### **Production Build**

```bash
npm run build
```

Output: `frontend/dist/`

**Build Optimizations:**
- Code splitting
- Tree shaking
- Minification
- Asset optimization

### **Preview Production Build**

```bash
npm run preview
```

### **Linting**

```bash
npm run lint
```

Uses ESLint with TypeScript rules.

### **Type Checking**

```bash
npx tsc --noEmit
```

Validates TypeScript types without emitting files.

### **Deployment Options**

**1. Static Hosting (Netlify, Vercel, GitHub Pages)**

```bash
npm run build
# Upload dist/ folder
```

**2. Docker**

```dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**3. Node.js Server**

```bash
npm install -g serve
serve -s dist -l 3000
```

### **Environment Variables**

Create `.env` file:

```bash
VITE_API_BASE_URL=http://localhost:8082
VITE_APP_NAME=Enterprise Workflow Task Management
```

Access in code:
```typescript
const apiUrl = import.meta.env.VITE_API_BASE_URL;
```

---

## Performance Optimization

### **Code Splitting**

React Router automatically splits routes:
```typescript
const ProjectsPage = lazy(() => import('./pages/projects/ProjectsPage'));
```

### **Memo & Callbacks**

```typescript
const MemoizedComponent = React.memo(ExpensiveComponent);

const memoizedCallback = useCallback(() => {
  doSomething();
}, [dependency]);

const memoizedValue = useMemo(() => computeExpensiveValue(), [dependency]);
```

### **Virtualization**

For large lists (1000+ items), use `react-virtual`:
```typescript
import { useVirtualizer } from '@tanstack/react-virtual';
```

### **Lazy Loading Images**

```tsx
<img loading="lazy" src={imageUrl} alt="..." />
```

---

## Testing

### **Unit Tests** (Vitest)

```bash
npm run test
```

**Example Test:**
```typescript
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Button } from '@/components/ui/button';

describe('Button', () => {
  it('renders with text', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });
});
```

### **E2E Tests** (Playwright - if implemented)

```bash
npx playwright test
```

---

## Troubleshooting

### **Common Issues**

**1. White screen on load**
- Check browser console for errors
- Verify API base URL is correct
- Check if backend is running

**2. Authentication not working**
- Clear `localStorage` (`authToken`)
- Verify JWT token format
- Check backend `/api/v1/auth/login` endpoint

**3. Dark mode not persisting**
- Check `localStorage` for `theme` key
- Verify `themeStore` is properly initialized

**4. Undo/Redo not working**
- Check `undoRedoStore` history
- Verify `addUndoAction` is called after operations
- Check console for errors in `undoFn`/`redoFn`

**5. TypeScript errors**
- Run `npx tsc --noEmit` to see all errors
- Check `types/index.ts` for missing interfaces
- Verify imports are correct

**6. Build fails**
- Clear `node_modules` and reinstall: `rm -rf node_modules && npm install`
- Check for peer dependency warnings
- Verify all imports exist

---

## Browser Support

| Browser | Version |
|---------|---------|
| Chrome | 90+ |
| Firefox | 88+ |
| Safari | 14+ |
| Edge | 90+ |

---

## Security Considerations

### **XSS Prevention**
- React automatically escapes all user input
- Use `dangerouslySetInnerHTML` only with sanitized HTML

### **CSRF Protection**
- JWT tokens in Authorization header (not cookies)
- No CSRF token needed

### **Sensitive Data**
- Never log tokens to console in production
- Don't store sensitive data in localStorage

### **HTTPS**
- Always use HTTPS in production
- Set `Secure` flag on cookies

---

## Support & Maintenance

**Contact:** Frontend Development Team  
**Documentation Last Updated:** February 12, 2026

For additional support, refer to inline JSDoc comments in the source code.
