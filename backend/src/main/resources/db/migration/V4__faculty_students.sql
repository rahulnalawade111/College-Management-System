-- =====================================================================
-- Phase 2 — Faculty, Students, Parents/Guardians, Enrollments
-- =====================================================================

CREATE TABLE faculty (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id      VARCHAR(30)  NOT NULL,
    first_name       VARCHAR(60)  NOT NULL,
    last_name        VARCHAR(60)  NOT NULL,
    email            VARCHAR(120) NOT NULL,
    phone            VARCHAR(20),
    date_of_birth    DATE,
    gender           VARCHAR(10),
    qualification    VARCHAR(120),
    experience_years INT,
    designation      VARCHAR(80),
    department_id    BIGINT       NOT NULL,
    joining_date     DATE,
    photo_url        VARCHAR(255),
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP(6) NOT NULL,
    updated_at       TIMESTAMP(6) NULL,
    CONSTRAINT uk_faculty_employee_id UNIQUE (employee_id),
    CONSTRAINT uk_faculty_email       UNIQUE (email),
    CONSTRAINT fk_faculty_department  FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT ck_faculty_status      CHECK (status IN ('ACTIVE','INACTIVE','ON_LEAVE','RETIRED')),
    CONSTRAINT ck_faculty_gender      CHECK (gender IS NULL OR gender IN ('MALE','FEMALE','OTHER'))
) ENGINE=InnoDB;

CREATE INDEX idx_faculty_department ON faculty(department_id);
CREATE INDEX idx_faculty_status     ON faculty(status);

CREATE TABLE students (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id       VARCHAR(30)  NOT NULL,          -- natural key e.g. ABC2026CS001
    first_name       VARCHAR(60)  NOT NULL,
    last_name        VARCHAR(60)  NOT NULL,
    email            VARCHAR(120) NOT NULL,
    phone            VARCHAR(20),
    date_of_birth    DATE,
    gender           VARCHAR(10),
    address_line1    VARCHAR(120),
    city             VARCHAR(60),
    state            VARCHAR(60),
    pincode          VARCHAR(10),
    photo_url        VARCHAR(255),
    admission_date   DATE,
    course_id        BIGINT       NOT NULL,
    department_id    BIGINT       NOT NULL,
    semester_id      BIGINT       NOT NULL,
    academic_year_id BIGINT       NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP(6) NOT NULL,
    updated_at       TIMESTAMP(6) NULL,
    CONSTRAINT uk_students_student_id UNIQUE (student_id),
    CONSTRAINT uk_students_email      UNIQUE (email),
    CONSTRAINT fk_students_course     FOREIGN KEY (course_id)     REFERENCES courses(id),
    CONSTRAINT fk_students_department FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_students_semester   FOREIGN KEY (semester_id)   REFERENCES semesters(id),
    CONSTRAINT fk_students_acyear     FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    CONSTRAINT ck_students_status     CHECK (status IN ('ACTIVE','INACTIVE','GRADUATED','SUSPENDED','TRANSFERRED')),
    CONSTRAINT ck_students_gender     CHECK (gender IS NULL OR gender IN ('MALE','FEMALE','OTHER'))
) ENGINE=InnoDB;

CREATE INDEX idx_students_department ON students(department_id);
CREATE INDEX idx_students_course     ON students(course_id);
CREATE INDEX idx_students_semester   ON students(semester_id);
CREATE INDEX idx_students_status     ON students(status);
CREATE INDEX idx_students_name       ON students(last_name, first_name);

CREATE TABLE parents (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT       NOT NULL,
    name       VARCHAR(120) NOT NULL,
    relation   VARCHAR(30)  NOT NULL DEFAULT 'FATHER',
    email      VARCHAR(120),
    phone      VARCHAR(20),
    occupation VARCHAR(80),
    address    VARCHAR(255),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NULL,
    CONSTRAINT fk_parents_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT ck_parents_relation CHECK (relation IN ('FATHER','MOTHER','GUARDIAN'))
) ENGINE=InnoDB;

CREATE INDEX idx_parents_student ON parents(student_id);

CREATE TABLE enrollments (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id       BIGINT       NOT NULL,
    course_id        BIGINT       NOT NULL,
    academic_year_id BIGINT       NOT NULL,
    semester_id      BIGINT       NOT NULL,
    enrollment_date  DATE         NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP(6) NOT NULL,
    updated_at       TIMESTAMP(6) NULL,
    CONSTRAINT uk_enrollments_unique UNIQUE (student_id, academic_year_id, semester_id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id)       REFERENCES students(id),
    CONSTRAINT fk_enrollments_course   FOREIGN KEY (course_id)       REFERENCES courses(id),
    CONSTRAINT fk_enrollments_acyear   FOREIGN KEY (academic_year_id) REFERENCES academic_years(id),
    CONSTRAINT fk_enrollments_semester FOREIGN KEY (semester_id)     REFERENCES semesters(id),
    CONSTRAINT ck_enrollments_status   CHECK (status IN ('ACTIVE','DROPPED','COMPLETED'))
) ENGINE=InnoDB;

CREATE INDEX idx_enrollments_student ON enrollments(student_id);

-- =====================================================================
-- Seed — 6 faculty members (one per dept) + 12 students + guardians
-- =====================================================================
INSERT INTO faculty (employee_id, first_name, last_name, email, phone, date_of_birth, gender,
                     qualification, experience_years, designation, department_id, joining_date, status, created_at) VALUES
('FAC001', 'Rajesh', 'Kumar',    'rajesh.kumar@college.edu',   '9876500001', '1980-03-12', 'MALE',   'Ph.D. Computer Science',    15, 'Professor',      1, '2012-06-01', 'ACTIVE', NOW()),
('FAC002', 'Sunita', 'Sharma',   'sunita.sharma@college.edu',  '9876500002', '1985-07-25', 'FEMALE', 'M.Tech IT',                12, 'Associate Prof', 2, '2013-07-01', 'ACTIVE', NOW()),
('FAC003', 'Amit',   'Desai',    'amit.desai@college.edu',     '9876500003', '1988-01-18', 'MALE',   'M.Sc Electronics',         10, 'Assistant Prof', 3, '2015-06-15', 'ACTIVE', NOW()),
('FAC004', 'Priya',  'Menon',    'priya.menon@college.edu',    '9876500004', '1990-09-30', 'FEMALE', 'MBA Finance',              8,  'Assistant Prof', 4, '2017-01-10', 'ACTIVE', NOW()),
('FAC005', 'Vikram', 'Singh',    'vikram.singh@college.edu',   '9876500005', '1983-11-05', 'MALE',   'M.A. English, B.Ed',       13, 'Associate Prof', 5, '2014-06-20', 'ACTIVE', NOW()),
('FAC006', 'Kavita', 'Patil',    'kavita.patil@college.edu',   '9876500006', '1991-04-14', 'FEMALE', 'M.Sc Mathematics',         7,  'Lecturer',       6, '2018-07-05', 'ACTIVE', NOW());

UPDATE departments SET hod_id = 1 WHERE id = 1;
UPDATE departments SET hod_id = 2 WHERE id = 2;
UPDATE departments SET hod_id = 3 WHERE id = 3;

-- Password for all seeded students: Student@123 (BCrypt, cost 10)
INSERT INTO students (student_id, first_name, last_name, email, phone, date_of_birth, gender,
                      address_line1, city, state, pincode, admission_date, course_id, department_id,
                      semester_id, academic_year_id, status, created_at) VALUES
('ABC2026CS001', 'Aarav',   'Shah',     'aarav.shah@college.edu',      '9811100001', '2006-02-10', 'MALE',   '12 MG Road',        'Pune',     'MH', '411001', '2026-06-15', 1, 1, 1, 1, 'ACTIVE', NOW()),
('ABC2026CS002', 'Diya',    'Patel',    'diya.patel@college.edu',      '9811100002', '2005-08-22', 'FEMALE', '45 Lake View',      'Ahmedabad','GJ', '380015', '2026-06-15', 1, 1, 1, 1, 'ACTIVE', NOW()),
('ABC2026CS003', 'Rohan',   'Verma',    'rohan.verma@college.edu',     '9811100003', '2006-05-04', 'MALE',   '8 Station Rd',      'Mumbai',   'MH', '400070', '2026-06-16', 1, 1, 1, 1, 'ACTIVE', NOW()),
('ABC2026CS004', 'Ananya',  'Iyer',     'ananya.iyer@college.edu',     '9811100004', '2005-12-01', 'FEMALE', '23 Brigade Ln',     'Bengaluru','KA', '560001', '2026-06-16', 1, 1, 1, 1, 'ACTIVE', NOW()),
('ABC2026IT001', 'Kabir',   'Malhotra', 'kabir.malhotra@college.edu',  '9811100005', '2006-01-19', 'MALE',   '101 Park St',       'Kolkata',  'WB', '700016', '2026-06-15', 2, 2, 1, 1, 'ACTIVE', NOW()),
('ABC2026IT002', 'Ishita',  'Gupta',    'ishita.gupta@college.edu',    '9811100006', '2005-11-11', 'FEMALE', '67 Civil Lines',    'Delhi',    'DL', '110054', '2026-06-15', 2, 2, 1, 1, 'ACTIVE', NOW()),
('ABC2026EC001', 'Arjun',   'Nair',     'arjun.nair@college.edu',      '9811100007', '2006-04-25', 'MALE',   '5 Beach Rd',        'Kochi',    'KL', '682001', '2026-06-17', 3, 3, 1, 1, 'ACTIVE', NOW()),
('ABC2026EC002', 'Meera',   'Joshi',    'meera.joshi@college.edu',     '9811100008', '2005-09-07', 'FEMALE', '89 Temple St',      'Jaipur',   'RJ', '302001', '2026-06-17', 3, 3, 1, 1, 'ACTIVE', NOW()),
('ABC2026MG001', 'Farhan',  'Khan',     'farhan.khan@college.edu',     '9811100009', '2006-07-30', 'MALE',   '2 MG Road Cross',   'Hyderabad','TG', '500002', '2026-06-18', 4, 4, 1, 1, 'ACTIVE', NOW()),
('ABC2026MG002', 'Sneha',   'Reddy',    'sneha.reddy@college.edu',     '9811100010', '2005-10-16', 'FEMALE', '34 Jubilee Hills',  'Hyderabad','TG', '500033', '2026-06-18', 4, 4, 1, 1, 'ACTIVE', NOW()),
('ABC2026BA001', 'Tanvi',   'Deshmukh', 'tanvi.deshmukh@college.edu',  '9811100011', '2006-03-08', 'FEMALE', '56 FC Road',        'Pune',     'MH', '411005', '2026-06-19', 5, 5, 1, 1, 'ACTIVE', NOW()),
('ABC2026SC001', 'Nikhil',  'Rao',      'nikhil.rao@college.edu',      '9811100012', '2005-06-21', 'MALE',   '78 Science City',   'Ahmedabad','GJ', '380060', '2026-06-19', 6, 6, 1, 1, 'ACTIVE', NOW());

INSERT INTO enrollments (student_id, course_id, academic_year_id, semester_id, enrollment_date, status, created_at)
SELECT s.id, s.course_id, s.academic_year_id, s.semester_id, s.admission_date, 'ACTIVE', NOW()
FROM students s;

-- Guardians for every student (father + mother for the first two, father for the rest)
INSERT INTO parents (student_id, name, relation, email, phone, occupation, address, created_at)
SELECT s.id, CONCAT(s.first_name, '''s Father'), 'FATHER',
       REPLACE(REPLACE(LOWER(s.email), SUBSTRING_INDEX(s.email, '@', 1), CONCAT('parent.', LOWER(s.student_id))), '@college.edu', '@parent.edu'),
       CONCAT('98', RIGHT(s.phone, 9)), 'Service', s.address_line1, NOW()
FROM students s;

INSERT INTO parents (student_id, name, relation, email, phone, occupation, address, created_at)
SELECT s.id, CONCAT(s.first_name, '''s Mother'), 'MOTHER',
       REPLACE(REPLACE(LOWER(s.email), SUBSTRING_INDEX(s.email, '@', 1), CONCAT('mother.', LOWER(s.student_id))), '@college.edu', '@parent.edu'),
       CONCAT('98', RIGHT(s.phone, 9) + 1), 'Homemaker', s.address_line1, NOW()
FROM students s WHERE s.student_id IN ('ABC2026CS001','ABC2026CS002');
