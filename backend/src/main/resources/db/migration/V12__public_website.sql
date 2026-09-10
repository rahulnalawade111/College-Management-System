-- Phase 9: public website content (notices, events, contact messages)

CREATE TABLE notices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    body TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    published_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NULL,
    event_date DATE NOT NULL,
    venue VARCHAR(200) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    INDEX idx_events_status_date (status, event_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE contact_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL,
    phone VARCHAR(30) NULL,
    message TEXT NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'NEW',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_contact_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed published notices + events so the public homepage has live content
INSERT INTO notices (title, body, status, published_at) VALUES
('Admissions open for 2026–27', 'Applications are now open for all undergraduate and postgraduate programs. Apply before 30 June 2026 to be considered for merit scholarships.', 'PUBLISHED', NOW()),
('Annual Tech Fest — TechnoVerse 2026', 'Three days of hackathons, robotics, coding contests and expert talks. Registration is free for all enrolled students.', 'PUBLISHED', NOW()),
('Semester examination timetable released', 'The provisional timetable for the even-semester examinations is available on the student portal. Please report any clash to your department office within 7 days.', 'PUBLISHED', NOW());

INSERT INTO events (title, description, event_date, venue, status) VALUES
('TechnoVerse 2026 — Annual Tech Fest', 'Hackathons, robotics, coding contests and expert talks across three days.', '2026-10-12', 'Main Auditorium', 'PUBLISHED'),
('Open House & Campus Tour', 'Prospective students and parents are invited to tour the campus and meet faculty.', '2026-11-05', 'Administration Block', 'PUBLISHED'),
('Convocation Ceremony', 'Degree distribution for the graduating class of 2026.', '2026-12-18', 'Central Lawn', 'PUBLISHED');
