-- Users table — Phase 1 baseline
CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    email       VARCHAR(120) NOT NULL,
    password    VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP(6) NOT NULL,
    updated_at  TIMESTAMP(6) NULL,
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email    UNIQUE (email)
) ENGINE=InnoDB;

CREATE INDEX idx_users_role ON users(role);

-- =====================================================================
-- DEVELOPMENT / DEMO SEED ACCOUNTS (BCrypt hashes, cost 10)
--   admin   / Admin@123    → SUPER_ADMIN
--   admin2  / Admin@123    → ADMIN
--   faculty / Faculty@123  → FACULTY
--   student / Student@123  → STUDENT
--   parent  / Parent@123   → PARENT
-- ⚠ Demo credentials — MUST be changed before production use.
-- =====================================================================

INSERT INTO users (username, email, password, role, enabled, created_at) VALUES
('admin',   'admin@college.edu',   '$2a$10$BbLBrwtpPDJexEp..XHlJeL7atGYVDoZvk.jKN8WzA0tDntiLgLb.', 'SUPER_ADMIN', TRUE, NOW()),
('admin2',  'admin2@college.edu',  '$2a$10$BbLBrwtpPDJexEp..XHlJeL7atGYVDoZvk.jKN8WzA0tDntiLgLb.', 'ADMIN',       TRUE, NOW()),
('faculty', 'faculty@college.edu', '$2a$10$CYAPFRRcxzca/GebJgQnC.o/wVz/OqcUMD92MAnfrwzKNMog6jxKu', 'FACULTY',     TRUE, NOW()),
('student', 'student@college.edu', '$2a$10$Pxui0MzAwuK4w4I5SNKx2eakWcgx4Ql2OZxUc7rnvb6gYZWPmGyYC', 'STUDENT',     TRUE, NOW()),
('parent',  'parent@college.edu',  '$2a$10$hSZOTV/c1FmeSz7B0dbQi.DL8bAcPasNj9hd5MKjcp0VFpjOBPDb.', 'PARENT',      TRUE, NOW());
