-- =====================================================================
-- Phase 5 — Fees: fee structures, invoices, payments
-- =====================================================================

CREATE TABLE fees (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id     BIGINT        NOT NULL,
    title          VARCHAR(150)  NOT NULL,
    fee_type       VARCHAR(20)   NOT NULL,           -- TUITION | EXAM | HOSTEL | LIBRARY | TRANSPORT | MISC
    amount         DECIMAL(10,2) NOT NULL,
    paid_amount    DECIMAL(10,2) NOT NULL DEFAULT 0,
    due_date       DATE          NOT NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'PENDING',  -- PENDING | PARTIALLY_PAID | PAID | OVERDUE
    payment_method VARCHAR(30)   NULL,               -- CASH | UPI | BANK_TRANSFER | CARD | CHEQUE
    paid_at        TIMESTAMP(6)  NULL,
    academic_year_id BIGINT      NULL,
    created_at     TIMESTAMP(6)  NOT NULL,
    updated_at     TIMESTAMP(6)  NULL,
    CONSTRAINT fk_fee_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_fee_year    FOREIGN KEY (academic_year_id) REFERENCES academic_years(id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE INDEX idx_fee_student ON fees(student_id);
CREATE INDEX idx_fee_status  ON fees(status);

-- Demo fees: semester tuition for the three demo students
INSERT INTO fees (student_id, title, fee_type, amount, paid_amount, due_date, status, academic_year_id, created_at)
SELECT 1, 'Tuition Fee — Sem 1', 'TUITION', 45000, 45000, '2026-08-15', 'PAID', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM fees WHERE student_id = 1 AND title = 'Tuition Fee — Sem 1');

INSERT INTO fees (student_id, title, fee_type, amount, paid_amount, due_date, status, academic_year_id, created_at)
SELECT 1, 'Exam Fee — Midterm', 'EXAM', 1500, 0, '2026-10-10', 'PENDING', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM fees WHERE student_id = 1 AND title = 'Exam Fee — Midterm');

INSERT INTO fees (student_id, title, fee_type, amount, paid_amount, due_date, status, academic_year_id, created_at)
SELECT 2, 'Tuition Fee — Sem 1', 'TUITION', 45000, 20000, '2026-08-15', 'PARTIALLY_PAID', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM fees WHERE student_id = 2 AND title = 'Tuition Fee — Sem 1');

INSERT INTO fees (student_id, title, fee_type, amount, paid_amount, due_date, status, academic_year_id, created_at)
SELECT 3, 'Tuition Fee — Sem 1', 'TUITION', 45000, 0, '2026-07-30', 'OVERDUE', 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM fees WHERE student_id = 3 AND title = 'Tuition Fee — Sem 1');
