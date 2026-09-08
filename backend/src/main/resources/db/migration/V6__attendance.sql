-- =====================================================================
-- Phase 3 — Attendance
-- =====================================================================

CREATE TABLE attendance (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id      BIGINT       NOT NULL,
    subject_id      BIGINT       NOT NULL,
    faculty_id      BIGINT       NULL,
    attendance_date DATE         NOT NULL,
    status          VARCHAR(10)  NOT NULL,
    remarks         VARCHAR(255) NULL,
    created_at      TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) NULL,
    CONSTRAINT uk_attendance_unique UNIQUE (student_id, subject_id, attendance_date),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE SET NULL,
    CONSTRAINT ck_attendance_status  CHECK (status IN ('PRESENT','ABSENT','LATE','EXCUSED'))
) ENGINE=InnoDB;

CREATE INDEX idx_attendance_student_date ON attendance(student_id, attendance_date);
CREATE INDEX idx_attendance_subject_date  ON attendance(subject_id, attendance_date);
CREATE INDEX idx_attendance_date          ON attendance(attendance_date);

-- Demo attendance history: last 10 weekdays for BCA sem-1 subjects (ids 1..4)
-- PRESENT 80% / ABSENT 12% / LATE 5% / EXCUSED 3% distribution.
INSERT INTO attendance (student_id, subject_id, faculty_id, attendance_date, status, created_at)
SELECT s.id,
       sub.id,
       sub.faculty_id,
       d.d,
       CASE
         WHEN ((s.id * 31 + sub.id * 17 + DAY(d.d) * 7) % 100) < 80 THEN 'PRESENT'
         WHEN ((s.id * 31 + sub.id * 17 + DAY(d.d) * 7) % 100) < 92 THEN 'ABSENT'
         WHEN ((s.id * 31 + sub.id * 17 + DAY(d.d) * 7) % 100) < 97 THEN 'LATE'
         ELSE 'EXCUSED'
       END,
       NOW()
FROM students s
JOIN subjects sub ON sub.semester_id = s.semester_id AND sub.course_id = s.course_id
JOIN (
    SELECT DATE('2026-08-31') - INTERVAL n DAY AS d
    FROM (SELECT 0 n UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
          UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9
          UNION SELECT 10 UNION SELECT 11 UNION SELECT 12 UNION SELECT 13) nums
    WHERE WEEKDAY(DATE('2026-08-31') - INTERVAL n DAY) < 5
) d ON 1=1
WHERE s.status = 'ACTIVE';
