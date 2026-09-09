-- =====================================================================
-- Phase 4 — Assignments & submissions
-- =====================================================================

CREATE TABLE assignments (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_id     BIGINT        NOT NULL,
    faculty_id     BIGINT        NULL,
    title          VARCHAR(150)  NOT NULL,
    description    TEXT          NULL,
    assignment_type VARCHAR(20)  NOT NULL DEFAULT 'HOMEWORK',
    max_marks      INT           NOT NULL DEFAULT 100,
    due_date       TIMESTAMP(6)  NULL,
    attachment_url VARCHAR(500)  NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMP(6)  NOT NULL,
    updated_at     TIMESTAMP(6)  NULL,
    CONSTRAINT fk_assignment_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    CONSTRAINT fk_assignment_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE SET NULL,
    CONSTRAINT ck_assignment_type  CHECK (assignment_type IN ('HOMEWORK','LAB','PROJECT','REPORT')),
    CONSTRAINT ck_assignment_status CHECK (status IN ('ACTIVE','CLOSED'))
) ENGINE=InnoDB;

CREATE INDEX idx_assignment_subject ON assignments(subject_id);
CREATE INDEX idx_assignment_faculty ON assignments(faculty_id);
CREATE INDEX idx_assignment_due     ON assignments(due_date);

CREATE TABLE assignment_submissions (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    assignment_id  BIGINT        NOT NULL,
    student_id     BIGINT        NOT NULL,
    submitted_at   TIMESTAMP(6)  NOT NULL,
    submission_text TEXT         NULL,
    file_url       VARCHAR(500)  NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'SUBMITTED',
    marks_obtained DECIMAL(6,2)  NULL,
    feedback       VARCHAR(1000) NULL,
    graded_at      TIMESTAMP(6)  NULL,
    graded_by      BIGINT        NULL,
    created_at     TIMESTAMP(6)  NOT NULL,
    updated_at     TIMESTAMP(6)  NULL,
    CONSTRAINT uk_submission_unique UNIQUE (assignment_id, student_id),
    CONSTRAINT fk_submission_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_submission_student    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT ck_submission_status CHECK (status IN ('SUBMITTED','LATE','GRADED'))
) ENGINE=InnoDB;

CREATE INDEX idx_submission_student ON assignment_submissions(student_id);
CREATE INDEX idx_submission_assignment ON assignment_submissions(assignment_id);

-- =====================================================================
-- Demo data: one assignment per demo subject (ids 1..4), faculty 1
-- =====================================================================
INSERT INTO assignments (id, subject_id, faculty_id, title, description, assignment_type, max_marks, due_date, status, created_at)
SELECT 1, 1, 1, 'C Basics — Control Flow Worksheet',
       'Solve exercises 3.1–3.10 from chapter 3 and upload a single PDF.',
       'HOMEWORK', 20, DATE_ADD(NOW(), INTERVAL 7 DAY), 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM assignments WHERE id = 1);

INSERT INTO assignments (id, subject_id, faculty_id, title, description, assignment_type, max_marks, due_date, status, created_at)
SELECT 2, 2, 1, 'Logic Gates Lab Report',
       'Build the half-adder circuit and document truth tables with photos.',
       'LAB', 25, DATE_ADD(NOW(), INTERVAL 5 DAY), 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM assignments WHERE id = 2);

INSERT INTO assignment_submissions (id, assignment_id, student_id, submitted_at, submission_text, status, created_at)
SELECT 1, 1, 1, NOW() - INTERVAL 2 DAY, 'Completed all exercises, PDF attached in class portal.', 'SUBMITTED', NOW() - INTERVAL 2 DAY
WHERE NOT EXISTS (SELECT 1 FROM assignment_submissions WHERE id = 1);

INSERT INTO assignment_submissions (id, assignment_id, student_id, submitted_at, submission_text, status, created_at)
SELECT 2, 1, 2, NOW() - INTERVAL 1 DAY, 'Solutions for exercises 3.1 to 3.10.', 'SUBMITTED', NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM assignment_submissions WHERE id = 2);

INSERT INTO assignment_submissions (id, assignment_id, student_id, submitted_at, submission_text, status, created_at)
SELECT 3, 2, 1, NOW() - INTERVAL 1 DAY, 'Lab photos and truth table attached.', 'SUBMITTED', NOW() - INTERVAL 1 DAY
WHERE NOT EXISTS (SELECT 1 FROM assignment_submissions WHERE id = 3);
