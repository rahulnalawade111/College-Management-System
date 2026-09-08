-- =====================================================================
-- Phase 2 — login identity links: users.student_id / users.faculty_id
-- Person records created through the ERP get a login account; disabling
-- the person disables the account.
-- =====================================================================

ALTER TABLE users
    ADD COLUMN student_id BIGINT NULL AFTER role,
    ADD COLUMN faculty_id BIGINT NULL AFTER student_id;

ALTER TABLE users
    ADD CONSTRAINT fk_users_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_users_faculty FOREIGN KEY (faculty_id) REFERENCES faculty(id) ON DELETE SET NULL;

CREATE INDEX idx_users_student ON users(student_id);
CREATE INDEX idx_users_faculty ON users(faculty_id);

-- Link the existing demo faculty account to seeded faculty row 1
UPDATE users SET faculty_id = 1 WHERE username = 'faculty';
