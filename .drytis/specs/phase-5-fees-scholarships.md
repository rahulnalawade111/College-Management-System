# Phase 5 — Fees, payments, scholarships

## Goal
Complete fee lifecycle: fee structures per course+academic year, per-student fee records,
payment collection with unique receipts and a mock gateway, scholarship management and
student application workflow.

## Files
- backend V6: fee_structures, student_fees, payments, scholarships,
  scholarship_applications (+ seed).
- PaymentGateway interface + MockPaymentGateway (simulated success/failure, configurable),
  ReceiptNumberService (RCP-YYYY-######, unique, retry-safe), MailService used for receipts
  (logging mock).
- Endpoints: FeeStructure CRUD (admin), GET /api/fees/student/{id} & /api/fees/me,
  POST /api/fees (assign fee to student / generate for course-year), POST /api/payments
  (records payment, updates paid/pending/status, emits audit + notification),
  GET /api/payments/student/{id} & /me (history), admin fee-collection dashboard stats
  (collected vs pending by month/department), Scholarship CRUD, student apply w/ document
  upload, admin approve/reject with remarks, student track status.
- Frontend: admin Fees page (structures list/editor, assign to course-year bulk generation,
  collection dashboard with charts), Payments page (record cash/card/UPI/transfer/online,
  receipt view/print), Student Fees page (total/paid/pending/due date cards, pay-now via
  mock gateway flow, payment history table), Scholarships pages (admin review table,
  student apply + track).

## Acceptance criteria
- [ ] Assigning a fee structure to a course+academic year creates StudentFee rows with
      correct total/pending derived from the structure.
- [ ] Recording a payment updates paid_amount/pending_amount, changes status (PAID/PARTIAL/PENDING),
      and generates a unique receipt number visible in the UI and history.
- [ ] Over-payment beyond pending amount is rejected with a clear error.
- [ ] Student fee page shows their own totals and payment history only; parent sees child's.
- [ ] Admin fee dashboard totals equal the sum of payments in the DB for the selected period.
- [ ] Student can apply for a scholarship with a document upload and track PENDING →
      APPROVED/REJECTED; admin's decision updates the status with remarks visible to the student.
- [ ] Approved scholarship can be applied as a concession (adjustment shown in fee summary).

## Tests
FeeService generation math, PaymentService (amount validation, receipt uniqueness under
concurrency, pending recompute), ScholarshipApplication state transitions, authorization
(student cannot record payments for others; parent read-only).

## Edge cases
Multiple partial payments, payment for already-paid fee, receipt number sequence gaps,
due-date reminders (notification generated), scholarship deadline passed → apply blocked.
