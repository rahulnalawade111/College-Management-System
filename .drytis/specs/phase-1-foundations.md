# Phase 1 — Foundations: build tooling, database, auth, JWT, roles

## Goal
Runnable Spring Boot 3.x + MySQL + React skeleton with real JWT authentication for all
five roles, Flyway-managed schema baseline, and demo accounts.

## Setup (environment reality check)
- Verify/install JDK 17+ and Maven in the container (apt or sdkman); record exact steps in README.
- Scaffold `backend/` (Spring Initializr-style Maven project, package com.college.sms) and
  `frontend/` (Vite react). Register background services `sms-backend` (:8080) and
  `sms-frontend` (:5173). Caddy routes /api→8080, /→5173 already exist.

## Files
- backend: pom.xml (web, data-jpa, security, validation, mysql, flyway-core+mysql, lombok,
  jjwt, springdoc, tests), application.properties (env-var driven), CollegeManagementApplication,
  config/SecurityConfig, OpenApiConfig, CorsConfig, JpaAuditingConfig,
  security/ (JwtService, JwtAuthFilter, UserPrincipal), entity/User, repository/UserRepository,
  dto/ auth requests+responses, service/AuthService + UserService,
  controller/AuthController (/api/auth/login|register|forgot-password|reset-password|logout|me),
  exception/ (GlobalExceptionHandler + ResourceNotFound/Duplicate/BadRequest/Unauthorized),
  resources/db/migration/V1__users.sql + seed of demo accounts (BCrypt), MailService mock.
- frontend: Vite app, services/api.js (axios + interceptors), services/authService.js,
  context/AuthContext.jsx, routes/ProtectedRoute+RoleRoute, pages/auth/LoginPage (show/hide
  password, remember me, forgot link, loading, validation, error),
  pages/auth/ForgotPasswordPage, ResetPasswordPage, minimal DashboardLayout + role landing
  stubs (real dashboards come in Phase 3), theme tokens (palette from spec).

## Acceptance criteria
- [ ] Logging in as each demo account (admin/Admin@123, admin2, faculty, student, parent —
      credentials listed on the login page for the demo) lands on the correct role's dashboard area.
- [ ] Calling any protected API without a token returns 401 JSON in the agreed error shape.
- [ ] Calling an admin API with the student demo token returns 403 (backend-enforced).
- [ ] Wrong password shows a clear error on the login page; expired/invalid JWT redirects
      the React app to /login.
- [ ] Passwords in the users table are BCrypt hashes, not plain text.
- [ ] Forgot-password issues a dev-visible reset token and reset-password changes the password.
- [ ] Swagger UI opens and lists the auth endpoints.
- [ ] Frontend and backend run as registered background services; the app survives container pause.

## Tests
- AuthService: login success/wrong-password/disabled-user, token generation & claims.
- JwtService: parse/validate/expiry.
- SecurityConfig: role rules for /admin/**, /faculty/**, /student/**, /parent/**.
- React: AuthContext login flow, ProtectedRoute redirect when logged out.

## Edge cases
Duplicate username/email on register (409); disabled user login (403); malformed
Authorization header; token with wrong signature; CORS from the Vite origin; remember-me
extends token storage lifetime only (never weakens server validation).
