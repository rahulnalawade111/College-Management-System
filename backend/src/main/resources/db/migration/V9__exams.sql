-- =====================================================================
-- Phase 4 — Exams & exam schedules
-- =====================================================================

CREATE TABLE exams (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_name         VARCHAR(120) NOT NULL,
    exam_type         VARCHAR(20)  NOT NULL,
    academic_year_id  BIGINT       NOT NULL,
    semester_id       BIGINT       NULL,
    start_date        DATE         NOT NULL,
    end_date          DATE         NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    result_published  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP(6) NOT NULL,
    updated_at        TIMESTAMP(6) NULL,
    CONSTRAINT fk_exam_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    CONSTRAINT fk_exam_semester      FOREIGN KEY (semester_id) REFERENCES semesters(id),
    CONSTRAINT ck_exam_type   CHECK (exam_type IN ('INTERNAL','MIDTERM','FINAL','PRACTICAL')),
    CONSTRAINT ck_exam_status CHECK (status IN ('SCHEDULED','ONGOING','COMPLETED','CANCELLED')),
    CONSTRAINT ck_exam_dates  CHECK (end_date >= start_date)
) ENGINE=InnoDB;

CREATE INDEX idx_exam_dates ON exams(start_date, end_date);

CREATE TABLE exam_schedules (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id     BIGINT      NOT NULL,
    subject_id  BIGINT      NOT NULL,
    exam_date   DATE        NOT NULL,
    start_time  TIME        NOT NULL,
    end_time    TIME        NOT NULL,
    room        VARCHAR(50) NULL,
    max_marks   INT         NOT NULL DEFAULT 100,
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6) NULL,
    CONSTRAINT uk_exam_schedule_unique UNIQUE (exam_id, subject_id),
    CONSTRAINT fk_schedule_exam   FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    CONSTRAINT ck_schedule_times CHECK (end_time > start_time)
) ENGINE=InnoDB;

CREATE INDEX idx_schedule_date ON exam_schedules(exam_date);

-- =====================================================================
-- Demo data: one midterm for the active academic year
-- =====================================================================
INSERT INTO exams (id, exam_name, exam_type, academic_year_id, semester_id, start_date, end_date, status, created_at)
SELECT 1, 'Midterm Examinations — Sem 1', 'MIDTERM',
       (SELECT id FROM academic_years WHERE active = TRUE LIMIT 1),
       (SELECT id FROM semesters WHERE semester_number = 1 LIMIT 1),
       DATE_ADD(CURDATE(), INTERVAL 21 DAY), DATE_ADD(CURDATE(), INTERVAL 28 DAY),
       'SCHEDULED', NOW()
WHERE NOT EXISTS (SELECT 1 FROM exams WHERE id = 1);

INSERT INTO exam_schedules (id, exam_id, subject_id, exam_date, start_time, end_time, room, max_marks, created_at)
SELECT 1, 1, 1, DATE_ADD(CURDATE(), INTERVAL 21 DAY), '09:30:00', '12:30:00', 'Room 101', 50, NOW()
WHERE NOT EXISTS (SELECT 1 FROM exam_schedules WHERE id = 1);

INSERT INTO exam_schedules (id, exam_id, subject_id, exam_date, start_time, end_time, room, max_marks, created_at)
SELECT 2, 1, 2, DATE_ADD(CURDATE(), INTERVAL 23 DAY), '09:30:00', '12:30:00', 'Room 102', 50, NOW()
WHERE NOT EXISTS (SELECT 1 FROM exam_schedules WHERE id = 2);

INSERT INTO exam_schedules (id, exam_id, subject_id, exam_date, start_time, end_time, room, max_marks, created_at)
SELECT 3, 1, 3, DATE_ADD(CURDATE(), INTERVAL 25 DAY), '14:00:00', '16:00:00', 'Room 101', 50, NOW()
WHERE NOT EXISTS (SELECT 1 FROM exam_schedules WHERE id = 3);
