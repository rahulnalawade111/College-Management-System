# Phase 8 — Reports, audit logs, settings, testing, hardening

## Goal
Governance layer: exportable reports for every module, complete audit trail, editable
system settings, full test pass and security hardening.

## Files
- backend V9: audit_logs + settings tables.
- AuditService wired into: login, student create/update/deactivate/delete, faculty CRUD,
  marks update, result publish, fee payment, notice publish, user creation — recording
  user, action, module, entity, description, timestamp. Admin Audit Log viewer with
  filters (user/module/date).
- Reports (ReportController + ReportService): Student, Attendance, Fee, Payment, Admission,
  Faculty, Result, Library, Hostel, Scholarship reports — each with date/department/course
  filters; export PDF (OpenPDF), Excel (Apache POI), CSV (streaming); role-scoped data.
- Settings: key/value admin UI + API (fine rate, max upload size, allowed file types,
  current academic year, college info) validated before use.
- Users & Roles admin page (create user, assign role, enable/disable, reset password).
- Testing pass: raise backend coverage on auth, students, faculty, courses, attendance,
  results, fees, authorization; add React tests for auth flow and 2–3 key components
  (login form, data table, attendance page state handling).
- Hardening: JWT secret from env only; CORS locked to preview origin + localhost:5173;
  actuator restricted; rate-limit login attempts (simple in-memory limiter); password
  policy for new users; confirm no secrets in git (git-secrets style scan), .env.example.

## Acceptance criteria
- [ ] Performing key actions (login, add student, record payment, publish result) creates
      audit rows visible in the admin viewer with who/what/when.
- [ ] Each report returns correct filtered totals and downloads a non-empty PDF, XLSX and
      CSV with sensible column headers.
- [ ] Student/parent roles cannot access report or audit endpoints (403).
- [ ] Changing the fine rate in Settings changes library fine calculation on the next
      overdue return.
- [ ] `mvn test` passes with the full suite; frontend tests pass via npm script.
- [ ] Repeated failed logins get temporarily blocked (limiter) with a friendly message.
- [ ] No credential or secret value is committed to the repository.

## Tests
AuditService capture on each logged action, report filters produce expected subsets,
settings validation, limiter behavior (allow N then block), authorization matrix test.

## Edge cases
Very large report exports (streaming/pagination cap), audit for actions by SUPER_ADMIN,
timezone on timestamps, concurrent settings edits (last-write-wins documented).
