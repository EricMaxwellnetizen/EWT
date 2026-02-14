# Backend Technical Documentation
## Enterprise Workflow Task Management System

**Version:** 1.0.0  
**Framework:** Spring Boot 3.2.5  
**Java Version:** 17  
**Last Updated:** February 12, 2026

---

## Table of Contents
1. [System Overview](#system-overview)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [Database Schema](#database-schema)
5. [Core Modules](#core-modules)
6. [Security & Authentication](#security--authentication)
7. [API Endpoints](#api-endpoints)
8. [Business Logic](#business-logic)
9. [Audit Logging System](#audit-logging-system)
10. [Configuration](#configuration)
11. [Deployment](#deployment)

---

## System Overview

The Enterprise Workflow Task Management System is a comprehensive backend application built with Spring Boot that manages projects, epics, stories, users, clients, and SLA rules in a hierarchical workflow structure.

### Key Features
- **Hierarchical Task Management**: Projects → Epics → Stories
- **User Management**: Role-based access control with 5 access levels
- **Client Management**: Associate projects with clients
- **SLA Rules**: Define and track service level agreements
- **Audit Logging**: Automatic logging of all CRUD operations
- **Authentication**: JWT-based auth with OAuth2 Google support
- **File Management**: Upload and manage files
- **Notifications**: System-wide notification management
- **Analytics**: Comprehensive business intelligence endpoints

---

## Architecture

### **Layered Architecture**

```
┌─────────────────────────────────────┐
│         Controllers                  │  ← REST API Layer
├─────────────────────────────────────┤
│         Services                     │  ← Business Logic
├─────────────────────────────────────┤
│         Repositories (JPA)           │  ← Data Access
├─────────────────────────────────────┤
│         Database (MySQL)             │  ← Persistence
└─────────────────────────────────────┘
```

### **Cross-Cutting Concerns (AOP)**
- **Audit Aspect**: Intercepts all JPA save/delete operations
- **Exception Handling**: Global exception handler
- **Security**: JWT token validation on protected endpoints

---

## Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **Framework** | Spring Boot | 3.2.5 |
| **Language** | Java | 17 |
| **Database** | MySQL | 8.x |
| **ORM** | Spring Data JPA | 3.2.x |
| **Security** | Spring Security + JWT | 6.2.x |
| **Build Tool** | Maven | 3.x |
| **JSON Processing** | Jackson | 2.15.x |
| **Validation** | Jakarta Bean Validation | 3.0.x |

---

## Database Schema

### **Core Tables**

#### **ewt_users**
- `id` BIGINT (PK, AUTO_INCREMENT)
- `username` VARCHAR(50) UNIQUE NOT NULL
- `password` VARCHAR(255) NOT NULL (BCrypt hashed)
- `first_name` VARCHAR(50)
- `last_name` VARCHAR(50)
- `email` VARCHAR(100) UNIQUE NOT NULL
- `phone_number` VARCHAR(20)
- `access_level` INT NOT NULL (1-5)
- `role` VARCHAR(50) NOT NULL
- `department` VARCHAR(100)
- `job_title` VARCHAR(100)
- `joining_date` DATE
- `reporting_to_id` BIGINT (FK → ewt_users)
- `created_at` TIMESTAMP
- `updated_at` TIMESTAMP

**Access Levels:**
- `1` = USER (Basic access)
- `2` = EMPLOYEE (Task assignee)
- `3` = LEAD (Team lead)
- `4` = MANAGER (Project manager)
- `5` = ADMIN (Full system access)

#### **ewt_clients**
- `id` BIGINT (PK, AUTO_INCREMENT)
- `name` VARCHAR(100) NOT NULL
- `email` VARCHAR(100)
- `phone_number` VARCHAR(20)
- `address` VARCHAR(255)
- `created_at` TIMESTAMP
- `updated_at` TIMESTAMP

#### **ewt_projects**
- `id` BIGINT (PK, AUTO_INCREMENT)
- `name` VARCHAR(200) NOT NULL
- `description` TEXT
- `client_id` BIGINT (FK → ewt_clients)
- `manager_id` BIGINT (FK → ewt_users)
- `start_date` DATE
- `deadline` DATE
- `deliverables` TEXT
- `is_completed` BOOLEAN DEFAULT FALSE
- `created_at` TIMESTAMP
- `updated_at` TIMESTAMP

#### **ewt_epics**
- `id` BIGINT (PK, AUTO_INCREMENT)
- `name` VARCHAR(200) NOT NULL
- `description` TEXT
- `project_id` BIGINT (FK → ewt_projects)
- `manager_id` BIGINT (FK → ewt_users)
- `start_date` DATE
- `due_date` DATE
- `is_approved` BOOLEAN DEFAULT FALSE
- `is_completed` BOOLEAN DEFAULT FALSE
- `created_at` TIMESTAMP
- `updated_at` TIMESTAMP

#### **ewt_stories**
- `id` BIGINT (PK, AUTO_INCREMENT)
- `name` VARCHAR(200) NOT NULL
- `description` TEXT
- `epic_id` BIGINT (FK → ewt_epics)
- `assigned_to_id` BIGINT (FK → ewt_users)
- `status` VARCHAR(50) NOT NULL
- `priority` VARCHAR(20)
- `due_date` DATE
- `estimated_hours` DECIMAL(10,2)
- `actual_hours` DECIMAL(10,2)
- `is_approved` BOOLEAN DEFAULT FALSE
- `created_at` TIMESTAMP
- `updated_at` TIMESTAMP
- `completed_at` TIMESTAMP

**Story Statuses:** `TODO`, `IN_PROGRESS`, `REVIEW`, `DONE`

#### **ewt_audit_logs**
- `id` BIGINT (PK, AUTO_INCREMENT)
- `timestamp` TIMESTAMP NOT NULL
- `entity_type` VARCHAR(100) NOT NULL (e.g., "User", "Project")
- `entity_id` BIGINT NOT NULL
- `operation` VARCHAR(20) NOT NULL (CREATE, UPDATE, DELETE, READ)
- `username` VARCHAR(100)
- `ip_address` VARCHAR(50)
- `old_value` LONGTEXT (JSON snapshot before change)
- `new_value` LONGTEXT (JSON snapshot after change)
- `changes` LONGTEXT (Field-by-field diff JSON)
- `description` VARCHAR(500)

**Indexes:**
- `idx_audit_timestamp` ON (`timestamp`)
- `idx_audit_entity` ON (`entity_type`)
- `idx_audit_operation` ON (`operation`)
- `idx_audit_user` ON (`username`)

#### **ewt_sla_rules**
- `id` BIGINT (PK, AUTO_INCREMENT)
- `name` VARCHAR(200) NOT NULL
- `description` TEXT
- `priority_level` VARCHAR(20) NOT NULL
- `response_time_hours` INT NOT NULL
- `resolution_time_hours` INT NOT NULL
- `escalation_time_hours` INT
- `is_active` BOOLEAN DEFAULT TRUE
- `created_at` TIMESTAMP
- `updated_at` TIMESTAMP

#### **ewt_notifications**
- `id` BIGINT (PK, AUTO_INCREMENT)
- `user_id` BIGINT (FK → ewt_users)
- `message` TEXT NOT NULL
- `type` VARCHAR(50) (INFO, WARNING, ERROR, SUCCESS)
- `is_read` BOOLEAN DEFAULT FALSE
- `created_at` TIMESTAMP
- `read_at` TIMESTAMP

---

## Core Modules

### **1. User Management**

**Controller:** `UserController.java`  
**Service:** `UserService.java`  
**Repository:** `UserRepository.java`  
**Model:** `User.java`

**Key Features:**
- User CRUD operations
- Hierarchical reporting structure (manager → employee)
- Access level-based permissions
- Password change with old password verification
- Editable users based on access level hierarchy

**Access Control Rules:**
- Level 5 (Admin): Can manage all users
- Level 4 (Manager): Can manage users with level ≤ 3
- Level 3 (Lead): Can manage users with level ≤ 2
- Cannot edit users at same or higher access level

### **2. Project Management**

**Controller:** `ProjectController.java`  
**Service:** `ProjectService.java`  
**Repository:** `ProjectRepository.java`  
**Model:** `Project.java`

**Key Features:**
- Project CRUD with client and manager association
- Paginated project listing
- Manager-specific project filtering
- Document generation (DOCX export)
- Automatic completion cascade (all epics complete → project complete)

### **3. Epic Management**

**Controller:** `EpicController.java`  
**Service:** `EpicService.java`  
**Repository:** `EpicRepository.java`  
**Model:** `Epic.java`

**Key Features:**
- Epic CRUD within projects
- Manager assignment and approval workflow
- Progress tracking based on story completion
- Automatic completion cascade (all stories complete → epic complete → project complete)

### **4. Story Management**

**Controller:** `StoryController.java`  
**Service:** `StoryService.java`  
**Repository:** `StoryRepository.java`  
**Model:** `Story.java`

**Key Features:**
- Story CRUD within epics
- Status transitions (TODO → IN_PROGRESS → REVIEW → DONE)
- Time tracking (estimated vs actual hours)
- User assignment
- Approval workflow
- Completion triggers epic/project cascade

### **5. Authentication & Authorization**

**Controller:** `AuthController.java`  
**Service:** `AuthService.java`  
**Components:**
- `JwtTokenProvider.java` - JWT generation and validation
- `JwtAuthenticationFilter.java` - Request filter for token validation
- `SecurityConfig.java` - Spring Security configuration

**JWT Token Structure:**
```json
{
  "sub": "username",
  "roles": ["ROLE_MANAGER"],
  "userId": 123,
  "accessLevel": 4,
  "iat": 1234567890,
  "exp": 1234571490
}
```

**Token Expiry:** 1 hour  
**Refresh Strategy:** Frontend handles re-login on 401

**OAuth2 Google Integration:**
- Login with Google
- Auto-create user on first login
- Default access level: 1 (USER)

---

## Security & Authentication

### **Endpoint Security**

**Public Endpoints:**
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/oauth2/**`

**Protected Endpoints:**
- All other `/api/v1/**` endpoints require valid JWT token

### **Role-Based Access Control**

Implemented via `@PreAuthorize` annotations:

```java
@PreAuthorize("hasRole('ADMIN')")  // Level 5 only
@PreAuthorize("hasRole('MANAGER')") // Level 4+ 
@PreAuthorize("hasRole('EMPLOYEE')") // Level 2+
```

### **Password Security**

- Hashing: BCrypt with strength 10
- Minimum length: 8 characters
- No plaintext storage

---

## API Endpoints

### **Authentication**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/auth/login` | Login with credentials | No |
| POST | `/api/v1/auth/register` | Register new user | No |
| POST | `/api/v1/auth/logout` | Logout user | Yes |
| GET | `/api/v1/auth/oauth2/google` | OAuth2 Google login | No |

### **Users**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/user/get` | Get all users | Yes |
| GET | `/api/v1/user/paginated` | Get paginated users | Yes |
| GET | `/api/v1/user/{id}` | Get user by ID | Yes |
| GET | `/api/v1/user/me` | Get current user | Yes |
| POST | `/api/v1/user/create` | Create new user | Yes (Level 3+) |
| PUT | `/api/v1/user/{id}` | Update user | Yes |
| PUT | `/api/v1/user/me` | Update own profile | Yes |
| DELETE | `/api/v1/user/{id}` | Delete user | Yes (Level 4+) |
| GET | `/api/v1/user/editable` | Get editable users | Yes |
| GET | `/api/v1/user/can-edit/{id}` | Check edit permission | Yes |
| PUT | `/api/v1/user/me/password` | Change password | Yes |

### **Projects**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/project/get` | Get all projects | Yes |
| GET | `/api/v1/project/paginated` | Get paginated projects | Yes |
| GET | `/api/v1/project/{id}` | Get project by ID | Yes |
| POST | `/api/v1/project/create` | Create project | Yes (Level 4+) |
| PUT | `/api/v1/project/{id}` | Update project | Yes |
| DELETE | `/api/v1/project/{id}` | Delete project | Yes (Level 4+) |
| GET | `/api/v1/project/manager/{managerId}/download` | Download projects DOCX | Yes |

### **Epics**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/epic/get` | Get all epics | Yes |
| GET | `/api/v1/epic/{id}` | Get epic by ID | Yes |
| POST | `/api/v1/epic/create` | Create epic | Yes (Level 3+) |
| PUT | `/api/v1/epic/{id}` | Update epic | Yes |
| DELETE | `/api/v1/epic/{id}` | Delete epic | Yes (Level 3+) |

### **Stories**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/story/get` | Get all stories | Yes |
| GET | `/api/v1/story/{id}` | Get story by ID | Yes |
| POST | `/api/v1/story/create` | Create story | Yes (Level 2+) |
| PUT | `/api/v1/story/{id}` | Update story | Yes |
| PUT | `/api/v1/story/{id}/complete` | Mark story complete | Yes |
| DELETE | `/api/v1/story/{id}` | Delete story | Yes (Level 3+) |

### **Clients**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/client/get` | Get all clients | Yes |
| GET | `/api/v1/client/{id}` | Get client by ID | Yes |
| GET | `/api/v1/client/{id}/hierarchy` | Get client with full hierarchy | Yes |
| POST | `/api/v1/client/create` | Create client | Yes (Level 4+) |
| PUT | `/api/v1/client/{id}` | Update client | Yes |
| DELETE | `/api/v1/client/{id}` | Delete client | Yes (Level 4+) |

### **SLA Rules**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/sla/get` | Get all SLA rules | Yes |
| GET | `/api/v1/sla/{id}` | Get SLA by ID | Yes |
| POST | `/api/v1/sla/create` | Create SLA | Yes (Level 4+) |
| PUT | `/api/v1/sla/{id}` | Update SLA | Yes (Level 4+) |
| DELETE | `/api/v1/sla/{id}` | Delete SLA | Yes (Level 5) |

### **Notifications**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/notifications` | Get user's notifications | Yes |
| GET | `/api/v1/notifications/unread` | Get unread count | Yes |
| PUT | `/api/v1/notifications/{id}/read` | Mark as read | Yes |
| DELETE | `/api/v1/notifications/{id}` | Delete notification | Yes |
| DELETE | `/api/v1/notifications/clear` | Clear all notifications | Yes |

### **Analytics**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/analytics/dashboard` | Dashboard metrics | Yes |
| GET | `/api/v1/analytics/project/{id}` | Project analytics | Yes |
| GET | `/api/v1/analytics/team` | Team analytics | Yes (Level 4+) |
| GET | `/api/v1/analytics/workload` | Workload distribution | Yes (Level 4+) |
| GET | `/api/v1/analytics/project/{id}/risks` | Risk analysis | Yes |

### **Audit Logs**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/v1/admin/audit-logs` | Get all audit logs | Yes (Level 5) |
| GET | `/api/v1/admin/audit-logs/search` | Search with filters | Yes (Level 5) |
| GET | `/api/v1/admin/audit-logs/{entityType}/{entityId}` | Get entity history | Yes (Level 5) |
| GET | `/api/v1/admin/audit-logs/recent` | Get recent 100 logs | Yes (Level 5) |
| GET | `/api/v1/admin/audit-logs/statistics` | Get statistics | Yes (Level 5) |

---

## Business Logic

### **Completion Cascade System**

When a story is marked as complete:
1. Check if all stories in the parent epic are complete
2. If yes → Mark epic as complete
3. Check if all epics in the parent project are complete
4. If yes → Mark project as complete

**Implementation:** `StoryService.completeStory()`

### **Access Level Hierarchy**

Users can only manage (create/edit/delete) users with **lower** access levels:

| User Level | Can Manage Levels |
|------------|-------------------|
| 5 (Admin) | 1, 2, 3, 4, 5 |
| 4 (Manager) | 1, 2, 3 |
| 3 (Lead) | 1, 2 |
| 2 (Employee) | None |
| 1 (User) | None |

**Implementation:** `UserService.canUserEdit()`

### **Project Assignment Rules**

- **Level 4 users**: Can assign themselves as managers OR assign Level 3 users
- **Level 5 users**: Can assign any manager (Level 3 or 4)

**Implementation:** `ProjectService.validateManagerAssignment()`

---

## Audit Logging System

### **How It Works**

The audit logging system uses **AOP (Aspect-Oriented Programming)** to automatically intercept all JPA repository operations.

**Components:**
- `AuditAspect.java` - AOP interceptor
- `AuditLogService.java` - Async logging service
- `AuditLog.java` - Entity model
- `AuditLogRepository.java` - Data access

### **Intercepted Operations**

```java
@Around("execution(* org.springframework.data.jpa.repository.JpaRepository+.save(..))")
@Around("execution(* org.springframework.data.jpa.repository.JpaRepository+.delete(..))")
@Around("execution(* org.springframework.data.jpa.repository.JpaRepository+.deleteById(..))")
```

### **What Gets Logged**

For every create, update, or delete:
- **Entity Type**: e.g., "User", "Project", "Story"
- **Entity ID**: The ID of the affected entity
- **Operation**: CREATE, UPDATE, or DELETE
- **Username**: Who performed the operation (from JWT)
- **IP Address**: Client IP (X-Forwarded-For or RemoteAddr)
- **Old Value**: JSON snapshot before change (UPDATE/DELETE)
- **New Value**: JSON snapshot after change (CREATE/UPDATE)
- **Changes**: Field-by-field diff (UPDATE only)
- **Timestamp**: When it happened

### **Example Audit Log Entry**

```json
{
  "id": 1234,
  "timestamp": "2026-02-12T10:30:00Z",
  "entityType": "Project",
  "entityId": 42,
  "operation": "UPDATE",
  "username": "john.doe",
  "ipAddress": "192.168.1.100",
  "oldValue": "{\"name\":\"Old Project Name\",\"deadline\":\"2026-03-01\"}",
  "newValue": "{\"name\":\"New Project Name\",\"deadline\":\"2026-03-15\"}",
  "changes": "{\"name\":\"Old Project Name → New Project Name\",\"deadline\":\"2026-03-01 → 2026-03-15\"}",
  "description": "Updated Project"
}
```

### **Querying Audit Logs**

**Get all logs for a specific entity:**
```
GET /api/v1/admin/audit-logs/Project/42
```

**Search with filters:**
```
GET /api/v1/admin/audit-logs/search?entityType=User&operation=DELETE&startDate=2026-02-01
```

**Get statistics:**
```
GET /api/v1/admin/audit-logs/statistics
```

Returns:
```json
{
  "byOperation": {
    "CREATE": 150,
    "UPDATE": 320,
    "DELETE": 25
  },
  "byEntityType": {
    "User": 80,
    "Project": 120,
    "Story": 295
  },
  "totalLogs": 495
}
```

---

## Configuration

### **application.properties**

```properties
# Server
server.port=8082

# Database
spring.datasource.url=jdbc:mysql://172.16.51.88:3306/hms?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=Root@123

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT
jwt.secret=your-256-bit-secret-key-here-must-be-at-least-32-characters
jwt.expiration=3600000

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# OAuth2 Google
spring.security.oauth2.client.registration.google.client-id=your-google-client-id
spring.security.oauth2.client.registration.google.client-secret=your-google-client-secret
spring.security.oauth2.client.registration.google.scope=profile,email

# Async
spring.task.execution.pool.core-size=5
spring.task.execution.pool.max-size=10
```

---

## Deployment

### **Prerequisites**
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### **Build**

```bash
cd backend
mvn clean package -DskipTests
```

Output: `target/EnterpriseWorkflowTask-0.0.1-SNAPSHOT.jar`

### **Run**

**Development:**
```bash
mvn spring-boot:run
```

**Production (detached):**
```powershell
Start-Process -FilePath "java" `
  -ArgumentList "-jar","target/EnterpriseWorkflowTask-0.0.1-SNAPSHOT.jar" `
  -WorkingDirectory "C:\path\to\backend" `
  -WindowStyle Hidden
```

**Docker:**
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### **Database Setup**

Run on first deployment:
```sql
CREATE DATABASE IF NOT EXISTS hms;
USE hms;
-- Tables will be auto-created by Hibernate
```

### **Health Check**

```bash
curl http://localhost:8082/actuator/health
```

---

## Error Handling

### **Global Exception Handler**

`GlobalExceptionHandler.java` handles all exceptions:

**Response Format:**
```json
{
  "timestamp": "2026-02-12T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/user/create",
  "errorCode": "VALIDATION_ERROR"
}
```

**Common Error Codes:**
- `VALIDATION_ERROR` - Invalid input
- `RESOURCE_NOT_FOUND` - Entity not found
- `UNAUTHORIZED` - Authentication failed
- `FORBIDDEN` - Insufficient permissions
- `CONFLICT` - Duplicate entry

---

## Performance Considerations

### **Database Indexes**

All foreign keys are indexed automatically. Additional indexes:
- `idx_audit_timestamp` - Fast audit log queries by time
- `idx_audit_entity` - Fast entity-specific audits
- `idx_audit_operation` - Filter by operation type

### **Async Processing**

Audit logging runs asynchronously (`@Async`) to avoid blocking main operations.

### **Pagination**

All "get all" endpoints support pagination:
```
GET /api/v1/user/paginated?page=0&size=20&sortBy=createdAt&sortDirection=DESC
```

---

## Monitoring & Logging

### **Logging Levels**

```properties
logging.level.root=INFO
logging.level.com.htc.enter=DEBUG
logging.level.org.springframework.security=DEBUG
```

### **Console Output**

- Request logs: `[API] ✓ Request WITH token: GET /api/v1/user/me`
- Audit logs: Errors logged to stderr, not thrown
- SQL logs: Disabled in production (`spring.jpa.show-sql=false`)

---

## Troubleshooting

### **Common Issues**

**401 Unauthorized on all requests:**
- Check JWT token in `Authorization: Bearer <token>` header
- Verify token hasn't expired (1 hour lifetime)
- Check `jwt.secret` matches between environments

**Audit logs not saving:**
- Check database `ewt_audit_logs` table exists
- Verify `spring.task.execution` config is correct
- Check logs for serialization errors

**User creation fails with 409 Conflict:**
- Username or email already exists
- Check `ewt_users` table for duplicates

**Database connection failed:**
- Verify MySQL is running
- Check `spring.datasource.url`, `username`, `password`
- Test connection: `mysql -h 172.16.51.88 -u root -p`

---

## Support & Maintenance

**Contact:** Development Team  
**Documentation Last Updated:** February 12, 2026

For additional support, refer to inline JavaDoc comments in the source code.
