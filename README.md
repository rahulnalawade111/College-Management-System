# College Management System (College ERP)

Full-stack College Student Management System / College ERP.

**Status: Phase 1 complete** — authentication foundation is live. Later phases (master data,
attendance, exams/results, fees, campus ops, public website) build on top of it.

## Stack
- **Backend**: Java 17 · Spring Boot 3.3 · Spring Security (JWT, BCrypt) · Spring Data JPA / Hibernate 6.6 · Flyway · MySQL · Bean Validation · Lombok · springdoc-openapi · JUnit + Mockito
- **Frontend**: React 18 (Vite) · React Router · Axios · Recharts · responsive CSS3

## Roles
SUPER_ADMIN · ADMIN · FACULTY · STUDENT · PARENT — enforced in Spring Security, mirrored in React routes.

## Modules (planned → see `.drytis/specs/`)
Auth & users ✅ · Students & guardians · Faculty · Departments/Courses/Subjects · Academic years & semesters · Attendance · Assignments · Exams & results (SGPA/CGPA) · Fees & payments · Scholarships · Library · Hostel · Transport · Notices · Events · Notifications · Documents · Reports · Audit logs · Public college website.

## Repository layout
- `backend/` — Spring Boot REST API (com.college.sms)
- `frontend/` — React SPA
- `.drytis/` — planning blueprint and phase specs

## Running locally
Prerequisites: JDK 17+, Maven 3.9+, Node 18+, MySQL 8.

```bash
# 1) Database: create schema (Flyway migrates on boot)
mysql -u root -e "CREATE DATABASE college_management"

# 2) Backend env — set at least these (see backend/src/main/resources/application.properties)
export DB_NAME=college_management DB_USERNAME=root DB_PASSWORD=secret
export JWT_SECRET=change-me-to-a-long-random-string-min-32-chars

# 3) Backend
cd backend && mvn spring-boot:run        # http://localhost:8080

# 4) Frontend (Vite proxies /api → :8080)
cd frontend && npm install && npm run dev # http://localhost:5173
```

Swagger UI: http://localhost:8080/swagger-ui.html

## Demo accounts (development only)
Seeded by Flyway. ⚠ Change every one of these before any production use.

| Username  | Password     | Role        |
|-----------|--------------|-------------|
| admin     | Admin@123    | SUPER_ADMIN |
| admin2    | Admin@123    | ADMIN       |
| faculty   | Faculty@123  | FACULTY     |
| student   | Student@123  | STUDENT     |
| parent    | Parent@123   | PARENT      |

## Environment variables
| Variable | Purpose |
|---|---|
| DB_HOST / DB_PORT / DB_NAME / DB_USERNAME / DB_PASSWORD | MySQL connection |
| JWT_SECRET | HS256 signing key (≥32 chars) |
| JWT_EXPIRATION_MS | Token TTL (default 24h) |
| FILE_UPLOAD_DIR | Private upload directory |
| CORS_ALLOWED_ORIGINS | Comma-separated allowed origins |

## API (Phase 1)
```
POST /api/auth/login            → { token, user { id, username, email, role } }
POST /api/auth/register         → self-registration (STUDENT role)
POST /api/auth/forgot-password  → dev mode returns reset token in response
POST /api/auth/reset-password   → { token, newPassword }
POST /api/auth/logout           → client discards token
GET  /api/auth/me               → current user (requires Bearer token)
```

Errors follow one shape: `{ timestamp, status, message, errors? }`.

## Tests
```bash
cd backend && mvn test   # unit tests for JwtService + AuthService
```
