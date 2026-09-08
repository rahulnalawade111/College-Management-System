# Scope — College ERP

## In scope (module → primary roles)
| Module | SUPER_ADMIN/ADMIN | FACULTY | STUDENT | PARENT |
|---|---|---|---|---|
| Auth & users | full | own profile | own profile | own profile |
| Departments/Courses/Subjects/AcademicYears/Semesters | CRUD | read | read | read |
| Students + guardians | CRUD, status changes | read (their courses) | own profile (limited edit) | linked child |
| Faculty | CRUD | own profile | read | — |
| Attendance | read/report | mark, bulk mark | own % view | child % view |
| Assignments/submissions | read | create/grade | view/submit | child view |
| Exams + schedules | CRUD | create (own subjects)/read | read | read |
| Results | enter/publish/lock | enter marks (own subjects) | own results, marksheet | child results |
| Fees/payments | structures, collect, dashboard | — | own fees, history | child fees |
| Scholarships | create/approve/reject | — | apply/track | child view |
| Library | books, transactions | read | issue/return view | — |
| Hostel | hostels/rooms/allocations | — | own allocation | — |
| Transport | buses/routes/allocations | — | own allocation | — |
| Notices | CRUD w/ targeting | read | relevant only | relevant only |
| Events | CRUD | read | register | read |
| Documents | verify | read (own subjects' students) | upload own | — |
| Notifications | own | own | own | own |
| Reports + exports | all | scoped | — | — |
| Audit log | view | — | — | — |

## Reports (Phase 8)
Student, Attendance, Fee, Payment, Admission, Faculty, Result, Library, Hostel,
Scholarship — filterable, exportable PDF/Excel/CSV.

## Public website (Phase 9)
/, /about, /academics, /departments, /courses, /admissions, /faculty, /events, /notices,
/gallery, /contact — notices & events fed live from backend; rest is CMS-less static
content in React. SEO meta + performance pass.

## Explicitly out of scope / mocked
- Real payment gateway (mock `PaymentGateway` service interface + in-memory/DB success impl).
- Real email sending (`MailService` interface + logging mock; forgot-password issues a
  dev-visible reset token).
- Cloud file storage (local private uploads directory, files served through an
  authorized controller endpoint, never from a public path).
- Timetable module: read-only simple weekly view fed from exam/subject data if not
  otherwise modeled — keep minimal, listed in sidebar but simple.

## Demo seed data (dev only)
admin/Admin@123 (SUPER_ADMIN), plus ADMIN, FACULTY, STUDENT, PARENT demo accounts;
7 departments, 6+ courses, subjects, academic years/semesters, a handful of students/
faculty, sample notices/events/books. Marked DEV ONLY; README requires password change.
