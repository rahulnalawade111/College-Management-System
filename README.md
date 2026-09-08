# College Management System (College ERP)

> **Status: planning blueprint — application build starting now (Phase 1).**
> This repository will receive the code phase by phase: backend/ (Spring Boot 3, Java 17)
> and frontend/ (React + Vite), as described in `.drytis/`.

Full-stack College Student Management System / College ERP.

## Stack (planned)
- **Backend**: Java 17 · Spring Boot 3.x · Spring Security (JWT, BCrypt) · Spring Data JPA / Hibernate · Flyway · MySQL 8 · Bean Validation · Lombok · springdoc-openapi · JUnit + Mockito
- **Frontend**: React 18 (Vite) · React Router · Axios · Recharts · responsive CSS3

## Roles
SUPER_ADMIN · ADMIN · FACULTY · STUDENT · PARENT — backend-enforced role-based authorization.

## Modules
Auth & users · Students & guardians · Faculty · Departments/Courses/Subjects · Academic years & semesters · Attendance · Assignments · Exams & results (SGPA/CGPA) · Fees & payments · Scholarships · Library · Hostel · Transport · Notices · Events · Notifications · Documents · Reports · Audit logs · Public college website.

## Repository layout
- `.drytis/` — planning blueprint: `spec.md`, `scope.md`, `schema.md`, `architecture.md`, `specs/` (phase-by-phase build specs)
- `backend/` — Spring Boot REST API *(arrives with Phase 1)*
- `frontend/` — React SPA *(arrives with Phase 1)*

## Running (once Phase 1 lands)
```
# backend (needs JDK 17+, MySQL)
cd backend && mvn clean install && mvn spring-boot:run   # :8080

# frontend
cd frontend && npm install && npm run dev                # :5173
```

Demo accounts (development only — change before production) will be seeded and listed
on the login page: admin/Admin@123 (SUPER_ADMIN), faculty, student, parent.
