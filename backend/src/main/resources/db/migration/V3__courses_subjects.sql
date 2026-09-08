-- Phase 2: courses, subjects (departments/years/semesters from V2)
CREATE TABLE courses (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code     VARCHAR(20)  NOT NULL,
    course_name     VARCHAR(120) NOT NULL,
    description     VARCHAR(500),
    duration        VARCHAR(20)  NOT NULL,
    degree_type     VARCHAR(30)  NOT NULL,
    department_id   BIGINT NOT NULL,
    total_semesters INT NOT NULL DEFAULT 6,
    fees            DECIMAL(12,2) NOT NULL DEFAULT 0,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) NULL,
    CONSTRAINT uk_courses_code UNIQUE (course_code),
    CONSTRAINT fk_courses_department FOREIGN KEY (department_id) REFERENCES departments(id)
) ENGINE=InnoDB;

-- faculty references departments (users exist from V1; faculty table itself comes in V4 with people)
CREATE TABLE subjects (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_code   VARCHAR(20)  NOT NULL,
    subject_name   VARCHAR(120) NOT NULL,
    credits        INT NOT NULL DEFAULT 4,
    semester_id    BIGINT NOT NULL,
    course_id      BIGINT NOT NULL,
    department_id  BIGINT NOT NULL,
    faculty_id     BIGINT NULL,          -- assigned in V4 (people)
    description    VARCHAR(500),
    created_at     TIMESTAMP(6) NOT NULL,
    updated_at     TIMESTAMP(6) NULL,
    CONSTRAINT uk_subjects_code UNIQUE (subject_code),
    CONSTRAINT fk_subjects_semester  FOREIGN KEY (semester_id)  REFERENCES semesters(id),
    CONSTRAINT fk_subjects_course    FOREIGN KEY (course_id)    REFERENCES courses(id),
    CONSTRAINT fk_subjects_department FOREIGN KEY (department_id) REFERENCES departments(id)
) ENGINE=InnoDB;

-- Seed: 6 courses
INSERT INTO courses (course_code, course_name, description, duration, degree_type, department_id, total_semesters, fees, status, created_at) VALUES
('BCA',  'Bachelor of Computer Applications', 'Application development, databases and IT fundamentals.', '3 Years',  'UG', 1, 6, 45000.00, 'ACTIVE', NOW()),
('BSCCS','BSc Computer Science',              'Core computing theory, algorithms and systems.',         '3 Years',  'UG', 1, 6, 48000.00, 'ACTIVE', NOW()),
('BBA',  'Bachelor of Business Administration','Management, marketing and organizational behaviour.',    '3 Years',  'UG', 4, 6, 40000.00, 'ACTIVE', NOW()),
('BCOM', 'Bachelor of Commerce',              'Accounting, taxation and financial management.',         '3 Years',  'UG', 3, 6, 38000.00, 'ACTIVE', NOW()),
('MCA',  'Master of Computer Applications',   'Advanced application engineering and architecture.',     '2 Years',  'PG', 1, 4, 72000.00, 'ACTIVE', NOW()),
('MSCCS','MSc Computer Science',              'Advanced computing, AI and research methods.',           '2 Years',  'PG', 1, 4, 76000.00, 'ACTIVE', NOW());

-- Seed: ~10 subjects (semester 1 & 2 of active academic year 2026-2027)
-- Active year is the first academic_years row; its semesters are ids 1..6
INSERT INTO subjects (subject_code, subject_name, credits, semester_id, course_id, department_id, description, created_at) VALUES
('BCA101', 'Programming in C',            4, 1, 1, 1, 'Fundamentals of procedural programming.', NOW()),
('BCA102', 'Digital Electronics',         4, 1, 1, 1, 'Logic gates, combinational circuits.', NOW()),
('BSC101', 'Discrete Mathematics',        4, 1, 2, 1, 'Sets, relations, graph theory.', NOW()),
('BSC102', 'Data Structures',             4, 1, 2, 1, 'Arrays, trees, graphs, complexity.', NOW()),
('BBA101', 'Principles of Management',    4, 1, 3, 4, 'Planning, organizing, leading.', NOW()),
('BCOM101','Financial Accounting',        4, 1, 4, 3, 'Ledgers, trial balance, statements.', NOW()),
('BCA201', 'Object Oriented Programming', 4, 2, 1, 1, 'Classes, inheritance, polymorphism in Java.', NOW()),
('BCA202', 'Database Management Systems', 4, 2, 1, 1, 'Relational model, SQL, normalization.', NOW()),
('BSC201', 'Operating Systems',           4, 2, 2, 1, 'Processes, memory, file systems.', NOW()),
('BBA201', 'Marketing Management',        4, 2, 3, 4, 'Segmentation, mix, strategy.', NOW());
