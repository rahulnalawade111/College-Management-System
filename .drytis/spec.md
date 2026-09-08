# Spec — College Management System (College ERP)

Source of truth: the user's full requirements document (also embedded in the project
proposal). This file records the binding decisions on top of it.

## Non-negotiables
- Two independent apps: `backend/` (Spring Boot 3.x, Java 17, Maven, package `com.college.sms`)
  and `frontend/` (React + Vite). Communication only via REST/JSON.
- Layered backend: Controller → Service → Repository. No business logic in controllers.
  DTOs for all requests/responses; entities never leave the service layer.
- Spring Security + JWT (`Authorization: Bearer`), BCrypt hashing, backend-enforced
  role security for SUPER_ADMIN / ADMIN / FACULTY / STUDENT / PARENT.
- MySQL (auto-provisioned in this environment) accessed via env vars:
  DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, JWT_SECRET — never hardcoded.
- Flyway migrations under `backend/src/main/resources/db/migration`; seed/demo data via
  Flyway or a CommandLineRunner, clearly marked as DEV ONLY.
- Global `@RestControllerAdvice` returning the agreed error JSON shape
  `{timestamp, status, message, errors}`.
- Frontend: Axios instance with JWT interceptor + 401 → clear auth → redirect /login;
  every API page has Loading/Empty/Error states + toasts.
- UI palette: primary #123B6D, secondary #1E5AA8, accent #F4B942, bg #F5F7FA,
  success #22C55E, warning #F59E0B, danger #EF4444. Academic ERP style, responsive
  down to mobile (drawer sidebar, hamburger nav, stacked cards, 1-col forms).
- No placeholder buttons/pages: every feature wired end-to-end (React → REST →
  Service → JPA → MySQL).
- Third-party needs (payments, email, cloud storage) behind service interfaces with
  development/mock implementations so the system runs locally.

## Environment decisions
- Container has Node.js + MySQL auto-provisioned. JDK 17 + Maven must be installed and
  verified as the first build step (apt/sdkman fallback), then backend runs on :8080 as
  a registered background service; frontend dev server on :5173. Caddy routes:
  `/api` → 8080, `/` → 5173 (already configured).
- React API base URL: same-origin `/api` in this environment (Vite dev proxy for local
  development per README: `http://localhost:8080/api`).
- Git: work committed continuously to the managed repo; final deliverable additionally
  pushed to GitHub (URL/token to be provided by the user before Phase 9 completes).

## Phase order (user-specified)
1 Foundations & auth · 2 Master data & people · 3 Dashboards & attendance ·
4 Subjects/assignments/exams/results · 5 Fees/payments/scholarships ·
6 Library/hostel/transport · 7 Notices/events/notifications/documents ·
8 Reports/audit/testing/hardening · 9 Public website/SEO/deploy/GitHub.

Phase specs: `/workspace/.drytis/specs/phase-1-foundations.md` … `phase-9-public-website.md`.
