-- Phase 2: master data — departments, academic years, semesters
CREATE TABLE departments (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_code  VARCHAR(20)  NOT NULL,
    department_name  VARCHAR(120) NOT NULL,
    description      VARCHAR(500),
    hod_id           BIGINT NULL,
    created_at       TIMESTAMP(6) NOT NULL,
    updated_at       TIMESTAMP(6) NULL,
    CONSTRAINT uk_departments_code UNIQUE (department_code)
) ENGINE=InnoDB;

CREATE TABLE academic_years (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    year_name   VARCHAR(20)  NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    active      BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_academic_years_name UNIQUE (year_name)
) ENGINE=InnoDB;

CREATE TABLE semesters (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    semester_number   INT NOT NULL,
    semester_name     VARCHAR(50) NOT NULL,
    academic_year_id  BIGINT NOT NULL,
    CONSTRAINT uk_semesters_year_number UNIQUE (academic_year_id, semester_number),
    CONSTRAINT fk_semesters_year FOREIGN KEY (academic_year_id) REFERENCES academic_years(id)
) ENGINE=InnoDB;

-- Seed: 7 departments
INSERT INTO departments (department_code, department_name, description, created_at) VALUES
('CS',  'Computer Science',          'Computing, software engineering and data science programs.', NOW()),
('IT',  'Information Technology',    'Networking, systems administration and cloud technologies.', NOW()),
('COM', 'Commerce',                  'Accounting, finance and commercial law.', NOW()),
('BBA', 'Business Administration',   'Management, marketing and entrepreneurship.', NOW()),
('ME',  'Mechanical Engineering',    'Thermodynamics, manufacturing and machine design.', NOW()),
('CE',  'Civil Engineering',         'Structures, geotechnics and transportation engineering.', NOW()),
('EC',  'Electronics',               'Circuits, embedded systems and signal processing.', NOW());

-- Seed: 2 academic years (2026-2027 active)
INSERT INTO academic_years (year_name, start_date, end_date, active) VALUES
('2026-2027', '2026-06-01', '2027-04-30', TRUE),
('2027-2028', '2027-06-01', '2028-04-30', FALSE);

-- Seed: semesters 1-6 for each academic year
INSERT INTO semesters (semester_number, semester_name, academic_year_id)
SELECT n.number,
       CONCAT('Semester ', n.number),
       ay.id
FROM academic_years ay
CROSS JOIN (SELECT 1 AS number UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6) n
ORDER BY ay.id, n.number;
