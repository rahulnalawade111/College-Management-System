# Schema — College ERP (MySQL: college_management)

Normalized relational model. All PKs `BIGINT AUTO_INCREMENT`. All FKs constrained,
indexes on FK columns and on frequently searched columns. Timestamps
`created_at`/`updated_at` (nullable on child tables where listed so in spec).
Flyway migrations: `V1__schema_core.sql` … sequenced per phase so the DB evolves with
the build. MySQL InnoDB, utf8mb4.

## Enums (stored as VARCHAR with app-level enums + CHECK-style validation in services)
Role: SUPER_ADMIN, ADMIN, FACULTY, STUDENT, PARENT
StudentStatus: ACTIVE, INACTIVE, GRADUATED, SUSPENDED, TRANSFERRED
AttendanceStatus: PRESENT, ABSENT, LATE, EXCUSED
SubmissionStatus: SUBMITTED, LATE, GRADED, NOT_SUBMITTED
ExamType: INTERNAL, MIDTERM, PRACTICAL, SEMESTER, FINAL
PaymentMethod: CASH, CARD, UPI, BANK_TRANSFER, ONLINE
DocumentType: ID_PROOF, MARKSHEET, CERTIFICATE, TRANSFER_CERTIFICATE, MIGRATION_CERTIFICATE, PHOTO, SIGNATURE, OTHER
Generic status enums (PENDING/ACTIVE/APPROVED/REJECTED/…/etc.) per entity as specified.

## Tables
- **users** — username UQ, email UQ, password (BCrypt), role, enabled, created_at, updated_at.
  Optional 1:1 links: student_id → students, faculty_id → faculty (login identity ↔ person).
- **departments** — department_code UQ, department_name, description, hod_id FK→faculty (nullable), timestamps.
- **academic_years** — year_name UQ (e.g. 2026-2027), start_date, end_date, active.
- **semesters** — semester_number, semester_name, academic_year_id FK; UQ(academic_year_id, semester_number).
- **courses** — course_code UQ, course_name, description, duration, degree_type, department_id FK,
  total_semesters, fees, status.
- **faculty** — employee_id UQ, names, email UQ, phone, dob, gender, qualification, experience,
  designation, department_id FK, joining_date, photo_url, status.
- **students** — student_id (natural, UQ), names, email, phone, dob, gender, address fields,
  photo_url, admission_date, course_id FK, department_id FK, semester_id FK,
  academic_year_id FK, status, timestamps.
- **parents** — name, email, phone, occupation, address, student_id FK.
- **subjects** — subject_code UQ, subject_name, credits, semester_id FK, course_id FK,
  department_id FK, faculty_id FK (nullable), description.
- **enrollments** — student_id, course_id, academic_year_id, semester_id, enrollment_date, status;
  UQ(student_id, academic_year_id, semester_id).
- **attendance** — student_id, subject_id, faculty_id, attendance_date, status, remarks;
  **UQ(student_id, subject_id, attendance_date)**.
- **assignments** — title, description, subject_id, faculty_id, course_id, semester_id,
  assigned_date, due_date, attachment_url, status.
- **assignment_submissions** — assignment_id, student_id, submission_date, file_url, remarks,
  marks, feedback, status.
- **exams** — exam_name, exam_type, academic_year_id, semester_id, start_date, end_date, status.
- **exam_schedules** — exam_id, subject_id, exam_date, start_time, end_time, room;
  UQ(exam_id, subject_id).
- **results** — student_id, exam_id, subject_id, internal_marks, external_marks, practical_marks,
  total_marks, grade, grade_point, credits, result_status; UQ(student_id, exam_id, subject_id);
  `published` flag → published rows locked from edit by non-publisher logic.
- **fee_structures** — course_id, academic_year_id, tuition/exam/library/hostel/transport/other/total fee;
  UQ(course_id, academic_year_id).
- **student_fees** — student_id, fee_structure_id, total_amount, paid_amount, pending_amount,
  due_date, status.
- **payments** — student_fee_id, transaction_id UQ, amount, payment_date, payment_method,
  receipt_number UQ (generated RCP-YYYY-######), status.
- **scholarships** — name, description, criteria, amount, deadline, status, total_slots.
- **scholarship_applications** — scholarship_id, student_id, applied_date, document_urls,
  status, review_remarks, reviewed_by.
- **books** — isbn UQ, title, author, publisher, category, total_copies, available_copies.
- **library_transactions** — book_id, student_id, issue_date, due_date, return_date, fine, status.
- **hostels** — name, type (BOYS/GIRLS), warden, contact, total_rooms.
- **rooms** — hostel_id, room_number, capacity, occupied, room_type; UQ(hostel_id, room_number).
- **hostel_allocations** — student_id, room_id, allocate_date, vacate_date, status, fee.
- **buses** — bus_number UQ, driver_name, driver_phone, capacity, status.
- **routes** — route_name, stops, fare.
- **transport_allocations** — student_id, bus_id, route_id, stop_name, allocate_date, status, fee.
- **notices** — title, description, category, created_by FK→users, publish_date, expiry_date,
  attachment_url, target_role, target_department_id, target_course_id, status.
- **events** — title, description, event_date, start_time, end_time, venue, image_url, organizer,
  registration_required, status.
- **event_registrations** — event_id, student_id, registered_at; UQ(event_id, student_id).
- **documents** — student_id, document_type, file_name, file_url, uploaded_at,
  verification_status, verified_by FK→users.
- **notifications** — user_id FK→users, title, message, type, read, created_at.
- **audit_logs** — user_id FK→users, action, module, entity_type, entity_id, timestamp, description;
  indexed on user_id, timestamp.
- **settings** (key/value) for fine rate/day, max file size, allowed types, etc.

## Grade computation
Standard 10-point scale used consistently:
≥90 → O/10, ≥80 → A+/9, ≥70 → A/8, ≥60 → B+/7, ≥50 → B/6, ≥45 → C/5, ≥40 → P/4, <40 → F/0.
SGPA = Σ(credits×grade_point)/Σ(credits) per semester; CGPA across semesters (same formula over all
completed, published results). Computed in `AcademicCalculationService`, unit-tested.

## Files
Uploaded files stored under `file.upload.directory` (outside public webroot), metadata in DB,
served ONLY through `GET /api/files/{...}` with authorization checks (own file / admin / faculty
of that subject). Allowed: jpg/png/pdf/doc(x)/xls(x); max 5 MB (configurable via settings).
