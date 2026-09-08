# Architecture — College ERP

```
Browser
  │  https://<preview> (Caddy)
  ├─ /  → Vite dev server :5173 (React SPA; prod: static build served by Caddy)
  └─ /api → Spring Boot :8080 (backend/college-sms)
                │ JPA/Hibernate
                └─ MySQL :3306 (auto-provisioned, creds via env vars)
```

## Backend layout (com.college.sms)
- `config/` — SecurityConfig (stateless, JWT filter, role rules), OpenApiConfig, CorsConfig,
  AsyncConfig, JpaAuditingConfig.
- `security/` — JwtService (sign/validate), JwtAuthFilter, UserPrincipal, Role-based
  method security (`@PreAuthorize`) as second layer.
- `controller/` — thin REST controllers per module; DTO in/out only.
- `dto/` — request/response records + PageResponse<T> wrapper.
- `entity/` — JPA entities per schema.md; Lombok.
- `repository/` — Spring Data + JpaSpecificationExecutor for filtered paging.
- `service/` — business logic incl. AcademicCalculationService, FileStorageService,
  ReceiptNumberService, AuditService; PaymentGateway + MailService interfaces with mock impls.
- `exception/` — GlobalExceptionHandler (@RestControllerAdvice) →
  `{timestamp, status, message, errors}`.

## Frontend layout (src/)
- `services/` — axios `api.js` (baseURL `/api`, JWT interceptor, 401 handler) + one file per domain.
- `context/` — AuthContext (login/logout/me, role), ToastContext.
- `hooks/` — useAuth, useFetch (loading/empty/error state machine), usePagination.
- `components/` — DataTable (search/sort/paginate/export), Modal, FormField, Badge, StatCard,
  ProgressBar, Chart wrappers (Recharts), EmptyState, ErrorState, Spinner, ConfirmDialog, FileUpload.
- `layouts/` — PublicLayout (site header/footer), AuthLayout, DashboardLayout (responsive sidebar).
- `routes/` — AppRoutes with ProtectedRoute/RoleRoute guards.
- `pages/` — public/, auth/, admin/, faculty/, student/, parent/.
- `utils/` — formatters, validators, grade helpers, csv export.

## Cross-cutting
- Audit: AuditService called from services (not controllers) for login, student CRUD,
  marks update, result publish, fee payment, notice publish, user creation.
- Errors: HTTP status codes 200/201/204/400/401/403/404/409/500 mapped consistently.
- Swagger UI at /api/swagger-ui (springdoc), JWT-authorized.
- Tests: JUnit5+Mockito service tests (auth, students, faculty, courses, attendance,
  results/calc, fees, authorization); React tests for auth flow & key components where practical.
- Background services (this environment): `sms-backend` (mvn spring-boot:run on :8080),
  `sms-frontend` (vite on :5173, --host). Never nohup/&.

## Build order rationale
Schema/auth first (everything depends on it), then master data & people (needed by every
module), then the highest-value workflows (dashboards, attendance), then academic
(assessments), money (fees/scholarships), infrastructure (library/hostel/transport),
communication (notices/events/docs), governance (reports/audit), finally the public site.
