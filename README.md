# College Management System (ABC College ERP)

A full-stack College / Student Management System: a public college website, role-based
portals for **Admin, Faculty, Student and Parent**, and modules for admissions, attendance,
assignments, exams, results (SGPA/CGPA + marksheet), and fees with receipts.

## Tech Stack

| Layer     | Technology |
|-----------|------------|
| Backend   | Java 17, Spring Boot 3.3, Spring Security (JWT), Spring Data JPA, Flyway, Swagger (springdoc) |
| Frontend  | React 19, Vite, React Router 7, Recharts |
| Database  | MySQL 8 / MariaDB 11 (Flyway migrations, `backend/src/main/resources/db/migration`) |

## Prerequisites

- **JDK 17** and **Maven 3.9+**
- **Node.js 18+** and npm
- **MySQL 8+** (or MariaDB 11+)

## 1. Database Setup

Create the database (schema and demo data are applied by Flyway on first boot):

```sql
CREATE DATABASE college_sms CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'sms_user'@'%' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON college_sms.* TO 'sms_user'@'%';
FLUSH PRIVILEGES;
```

## 2. Backend Setup

```bash
cd backend
cp .env.example .env        # or create .env manually (see table below)
mvn spring-boot:run          # dev; or: mvn clean package && java -jar target/college-management-0.1.0.jar
```

Backend starts on **http://localhost:8080**. Swagger UI: **http://localhost:8080/swagger-ui.html**

### Environment Variables (backend `.env`)

| Variable | Description | Example |
|---|---|---|
| `DB_HOST` | Database host | `127.0.0.1` |
| `DB_PORT` | Database port | `3306` |
| `DB_NAME` | Database name | `college_sms` |
| `DB_USER` | Database user | `sms_user` |
| `DB_PASSWORD` | Database password | `change-me` |
| `JWT_SECRET` | HMAC secret for JWT signing — **use a long random value** | `openssl rand -base64 64` |
| `FILE_UPLOAD_DIR` | Writable directory for uploaded files | `/var/lib/sms/uploads` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowlist | `http://localhost:5173` |

## 3. Frontend Setup

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173 (proxies /api → :8080)
```

Production build:

```bash
npm run build      # outputs frontend/dist — serve with any static server / Caddy / nginx
```

## 4. Demo Credentials (DEV ONLY — change before production!)

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `Admin@123` |
| Admin | `admin2` | `Admin@123` |
| Faculty | `faculty` | `Faculty@123` |
| Student | `student` | `Student@123` |
| Parent | `parent` | `Parent@123` |

> ⚠️ **These accounts exist only because Flyway seeds demo data. Delete/rotate them before
> any real deployment** (see `V1__users.sql` / `V7__demo_account_links.sql`).

## 5. API Documentation

Interactive OpenAPI docs are served by the backend at `/swagger-ui.html`
(JSON at `/v3/api-docs`). All authenticated endpoints use
`Authorization: Bearer <jwt>`; see the `Auth` endpoints to obtain a token.

## 6. Feature Map

- **Public website** — homepage (hero, stats, facilities, gallery), about, academics,
  departments, courses, admissions (with enquiry form), faculty, notices & events (live
  from the API), contact form, sitemap.xml, robots.txt, per-page SEO.
- **Admin portal** — departments, academic years, courses, subjects, faculty, students,
  exams, fees, website content (notices / events / contact inbox), dashboards & reports.
- **Faculty portal** — students, attendance marking, assignments & grading, results entry.
- **Student portal** — profile, attendance, assignments, exams, results & marksheet, fees.
- **Parent portal** — child's attendance, results, fees.

## 7. Production Deployment

1. Build both artifacts:
   ```bash
   cd backend  && mvn clean package -DskipTests
   cd frontend && npm ci && npm run build
   ```
2. Provide production env vars (DB creds, a strong `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`,
   `FILE_UPLOAD_DIR`) via your secret manager — never commit them.
3. Run the jar behind a reverse proxy; serve `frontend/dist` as static files and route
   `/api/*` to the backend (the included Caddyfile does exactly this).
4. Flyway applies pending migrations automatically on boot.
5. **Change all demo credentials and set a unique JWT secret before going live.**

## 8. Pushing to GitHub

```bash
git init
git remote add origin git@github.com:<you>/College-Management-System.git
git add .
git commit -m "Initial commit"
git push -u origin main
```

## Project Structure

```
backend/    Spring Boot app (controllers, services, entities, Flyway migrations, tests)
frontend/   React + Vite app (public pages, role portals, shared components)
```
