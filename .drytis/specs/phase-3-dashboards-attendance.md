# Phase 3 — Dashboards & attendance

## Goal
The three main dashboards (admin, faculty, student) fed by real aggregate APIs, plus the
full attendance workflow: faculty marks it, students/parents see color-coded percentages,
admin sees statistics.

## Files
- backend: attendance table (V4) + entity/repo/service/controller
  (POST /api/attendance bulk-mark list; GET /api/attendance/student/{id} summary+detail;
  GET /api/attendance/subject/{id}; GET /api/attendance/me & /attendance/my-summary for the
  logged-in student; admin aggregate endpoints for stats).
- DashboardController: /api/dashboard/admin (counts: students, faculty, departments, courses,
  new admissions, pending fees, today's attendance %, upcoming exams, recent activities),
  /api/dashboard/faculty, /api/dashboard/student, /api/dashboard/parent.
- frontend: DashboardLayout sidebar per role (admin sidebar items from requirements §36,
  responsive drawer), StatCard grid, Recharts (enrollment by department, monthly admissions,
  attendance stats, fee collection, exam performance), recent-activities feed.
- Attendance UI: faculty selects course/semester/subject/date → student list with
  PRESENT/ABSENT/LATE/EXCUSED per row, Mark All Present, Save; duplicate save prevented
  server-side by unique(student, subject, date) with a friendly 409 message.
- Student attendance page: per-subject table (total/present/absent/%), progress bars,
  green ≥80, yellow 60–79, red <60; same for parent (child's data).

## Acceptance criteria
- [ ] Admin dashboard numbers match the database (e.g. total students equals the students
      table count; today's attendance reflects rows saved today).
- [ ] Faculty can mark attendance for a subject+date and re-opening the same selection shows
      the saved values; saving twice for the same student/subject/date is rejected with a
      clear message.
- [ ] Student sees their own attendance % per subject with the correct color coding; they
      cannot see another student's attendance by changing IDs in the URL/API.
- [ ] Parent dashboard shows the linked child's attendance and fees summary.
- [ ] Dashboard charts render with real data (not hardcoded arrays).

## Tests
AttendanceService (bulk save, duplicate rejection, % computation, subject summary),
DashboardService aggregation counts; authorization test: student cannot call
/api/attendance/subject/{id}; parent can only read own child.

## Edge cases
Empty class list, attendance on a date with no timetable, EXCUSED not counting against %,
timezone consistency for "today".
