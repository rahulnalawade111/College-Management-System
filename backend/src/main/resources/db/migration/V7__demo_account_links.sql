-- =====================================================================
-- Phase 3 — link demo accounts to person rows + parent login link
-- =====================================================================

ALTER TABLE users
    ADD COLUMN parent_id BIGINT NULL AFTER faculty_id;

ALTER TABLE users
    ADD CONSTRAINT fk_users_parent FOREIGN KEY (parent_id) REFERENCES parents(id) ON DELETE SET NULL;

CREATE INDEX idx_users_parent ON users(parent_id);

-- Demo student account → Aarav Shah (students.id = 1)
UPDATE users SET student_id = 1 WHERE username = 'student';
-- Demo parent account → Aarav Shah's father (parents.id = 1)
UPDATE users SET parent_id = 1 WHERE username = 'parent';
