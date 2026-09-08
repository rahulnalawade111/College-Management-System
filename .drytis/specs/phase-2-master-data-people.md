# Phase 2 — Master data & people: departments, courses, subjects, years, semesters, faculty, students

## Goal
All organizational entities plus Faculty and Student management with full CRUD,
server-side search/filter/pagination, guardians, and the admin management screens.

## Files
- Flyway V2/V3: departments, academic_years, semesters, courses, subjects, faculty,
  students, parents, enrollments (+ seed: 7 departments, 6 courses, 2 academic years,
  semesters, ~10 subjects, demo faculty & students with enrollments).
- backend: entities, repositories (JpaSpecificationExecutor where filtering applies),
  DTOs, services, controllers + unit tests for: Department, Course, Subject, AcademicYear,
  Semester, Faculty, Student, Parent (nested in Student flow), Enrollment.
- StudentController: GET list w/ ?page&size&search&department&course&semester&status&sort,
  GET/{id}, POST, PUT/{id}, DELETE/{id} (soft-deactivate semantics honored),
  PATCH /{id}/status.
- Unique constraints: student_id natural key, employee_id, course_code, department_code,
  subject_code — duplicates → 409 with field-level error.
- frontend admin pages: Students (table w/ photo, ID, name, email, dept, course, semester,
  status, actions: view/edit/deactivate/delete; search, pagination, sorting, filters, CSV
  export, Add Student multi-section form incl. guardian section), Faculty, Departments,
  Courses, Subjects, Academic Years, Semesters; shared DataTable & form components.

## Acceptance criteria
- [ ] Adding a student through the admin form creates the row in MySQL and it appears in
      the paginated table without a manual refresh beyond normal refetch.
- [ ] Searching/filtering students by name/department/course/semester/status hits the
      backend (query params visible in network tab), not client-side filtering of a full list.
- [ ] Creating a duplicate student ID or course code shows a clear validation error (409)
      and nothing is saved.
- [ ] Deactivating a student changes their badge to INACTIVE and blocks that student's login.
- [ ] Departments/courses/subjects/years/semesters each support create/edit/delete from the UI
      with referential integrity protected (delete of a referenced department is refused with
      a friendly message).
- [ ] Faculty CRUD works end-to-end including photo upload.

## Tests
Service tests per module (create/update/delete/not-found/duplicate); StudentSpecification
filter test; controller slice test for query params; assignment of Hod to department.

## Edge cases
Pagination boundaries (page beyond last), empty search results, deleting records referenced
by FKs (block with 409), status transitions for students (ACTIVE→SUSPENDED etc. and the
login implication), guardians: one or more per student.
