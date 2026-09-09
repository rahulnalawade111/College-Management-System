-- =====================================================================
-- Phase 4 — Results: marks entry, grades, publication
-- =====================================================================

CREATE TABLE results (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id        BIGINT        NOT NULL,
    student_id     BIGINT        NOT NULL,
    subject_id     BIGINT        NOT NULL,
    internal_marks DECIMAL(5,2)  NULL,
    external_marks DECIMAL(5,2)  NULL,
    practical_marks DECIMAL(5,2) NULL,
    total_marks    DECIMAL(5,2)  NOT NULL,
    max_marks      INT           NOT NULL DEFAULT 100,
    grade          VARCHAR(3)    NOT NULL,
    grade_point    DECIMAL(3,1)  NOT NULL,
    credits        INT           NOT NULL DEFAULT 4,
    published      BOOLEAN       NOT NULL DEFAULT FALSE,
    published_at   TIMESTAMP(6)  NULL,
    created_at     TIMESTAMP(6)  NOT NULL,
    updated_at     TIMESTAMP(6)  NULL,
    CONSTRAINT uk_result_unique UNIQUE (exam_id, student_id, subject_id),
    CONSTRAINT fk_result_exam    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    CONSTRAINT fk_result_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_result_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_result_student ON results(student_id);
CREATE INDEX idx_result_exam    ON results(exam_id);

-- =====================================================================
-- Demo results: semester-1 subjects for demo student 1, published
-- =====================================================================
INSERT INTO results (exam_id, student_id, subject_id, internal_marks, external_marks, total_marks, max_marks, grade, grade_point, credits, published, published_at, created_at)
SELECT 1, 1, 1, 18, 62, 80, 100, 'A', 8.0, 4, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM results WHERE exam_id = 1 AND student_id = 1 AND subject_id = 1);

INSERT INTO results (exam_id, student_id, subject_id, internal_marks, external_marks, total_marks, max_marks, grade, grade_point, credits, published, published_at, created_at)
SELECT 1, 1, 2, 16, 55, 71, 100, 'A', 8.0, 4, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM results WHERE exam_id = 1 AND student_id = 1 AND subject_id = 2);

INSERT INTO results (exam_id, student_id, subject_id, internal_marks, external_marks, total_marks, max_marks, grade, grade_point, credits, published, published_at, created_at)
SELECT 1, 1, 3, 19, 70, 89, 100, 'O', 10.0, 4, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM results WHERE exam_id = 1 AND student_id = 1 AND subject_id = 3);

INSERT INTO results (exam_id, student_id, subject_id, internal_marks, external_marks, total_marks, max_marks, grade, grade_point, credits, published, published_at, created_at)
SELECT 1, 2, 1, 12, 40, 52, 100, 'C', 6.0, 4, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM results WHERE exam_id = 1 AND student_id = 2 AND subject_id = 1);

INSERT INTO results (exam_id, student_id, subject_id, internal_marks, external_marks, total_marks, max_marks, grade, grade_point, credits, published, published_at, created_at)
SELECT 1, 2, 2, 8, 30, 38, 100, 'F', 0.0, 4, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM results WHERE exam_id = 1 AND student_id = 2 AND subject_id = 2);
