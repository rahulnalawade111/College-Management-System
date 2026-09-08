# Phase 4 — Subjects, assignments, exams, results

## Goal
Academic operations: assignments & submissions, exams & schedules, marks entry with
grade/SGPA/CGPA computation, result publication + locking, student marksheet view/download.

## Files
- backend V5: assignments, assignment_submissions, exams, exam_schedules, results tables
  (+ seed sample data).
- Controllers/DTOs/services: Assignment (faculty create for own subjects, upload attachment,
  status), AssignmentSubmission (student submit before/after due → SUBMITTED/LATE, faculty
  grade → GRADED with marks+feedback), Exam CRUD + ExamSchedule (room/time per subject,
  UQ exam+subject), Result endpoints per requirements:
  GET /api/results/student/{studentId}, POST /api/results (bulk per subject allowed),
  PUT /api/results/{id}, POST /api/results/{id}/publish (ADMIN/SUPER_ADMIN only), plus
  GET /api/results/student/{id}/marksheet (PDF) and /api/results/me (student's own).
- AcademicCalculationService: total_marks = internal+external+practical; grade/grade_point
  via 10-point scale (see schema.md); SGPA per semester, CGPA overall; unit tests cover
  boundaries (90/80/70/60/50/45/40 and below).
- Locking: once published (or exam-level publish), results are read-only for everyone
  except a SUPER_ADMIN unpublish action; attempts → 409 with clear message.
- Marks entry authorization: ADMIN/FACULTY only; faculty restricted to own subjects.
- frontend: admin/faculty pages — Exams (+schedules editor), Assignments list/editor,
  Marks entry grid (students × internal/external/practical, auto total/grade preview),
  Results publish flow; student pages — Assignments (submit file), Exams (schedule view),
  Results (table + SGPA/CGPA cards + Download Marksheet); faculty dashboard grading queue.

## Acceptance criteria
- [ ] Faculty entering marks for their subject computes and stores total, grade and grade
      point correctly (spot-check a 92% → O/10, a 38% → F/0).
- [ ] Student results page shows SGPA for the semester and cumulative CGPA matching the
      stored credits and grade points.
- [ ] Publishing a result locks it: a faculty PUT afterwards returns 409 and the UI shows
      a "published/locked" state.
- [ ] Student cannot edit results through any endpoint; API rejects student-role writes.
- [ ] Marksheet PDF downloads with student name, exam, subjects, grades and SGPA/CGPA.
- [ ] Submitting an assignment after the due date records status LATE automatically.
- [ ] Exam schedule shows date/time/room per subject without double-booked subjects.

## Tests
AcademicCalculationService (grade boundaries, SGPA/CGPA math), ResultService publish/lock,
duplicate result row rejected, submission lateness, authorization matrix for marks entry.

## Edge cases
Missing component marks (nullable practical), 0-credit subjects excluded from SGPA,
re-publish after unpublish, PDF generation failure fallback, late submission grading rules.
