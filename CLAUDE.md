# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

### Backend (Java/Spring Boot)

```bash
# Build project
mvn clean compile

# Run application
mvn spring-boot:run

# Run tests
mvn test

# Package
mvn clean package -DskipTests

# Run single test class
mvn test -Dtest=UserControllerTest

# Run single test method
mvn test -Dtest=UserControllerTest#testListUsers

# Initialize database
mysql -u root -proot jguard < src/main/resources/schema.sql
```

### Frontend (Vue 3)

```bash
cd ui

# Install dependencies
npm install

# Run dev server (http://localhost:3000)
npm run dev

# Build for production
npm run build
```

## Architecture

Full-stack application: Spring Boot 3.2.0 + Java 17 REST API with Vue 3 + Element Plus frontend.

### Backend Tech Stack
- **Spring Boot 3.x** - Uses Jakarta EE 9+ namespace (`jakarta.*` instead of `javax.*`)
- **MyBatis 3.x** - XML mapper files in `src/main/resources/mapper/`
- **PageHelper** - Pagination plugin, `PageHelper.startPage()` must be called immediately before query
- **JWT (jjwt)** - Token-based authentication with BCrypt password hashing
- **Redis** - Distributed rate limiting with Lua scripts (graceful degradation when unavailable)
- **OpenTelemetry** - Distributed tracing with Micrometer Tracing bridge (traceId/spanId in MDC)
- **Springdoc OpenAPI 2.x** - Swagger UI at `/swagger-ui.html`

### Frontend Tech Stack
- **Vue 3** - Composition API
- **Element Plus** - UI components
- **Vite** - Build tool, dev server on port 3000 with API proxy to backend
- **Pinia** - State management (user token storage)
- **Vue Router** - Navigation with auth guard

### Backend Package Structure
```
com.jguard/
├── annotation/     # Custom annotations (@RateLimit)
├── aspect/         # AOP aspects (performance, rate limiting)
├── common/         # Result wrapper
├── config/         # Spring configs (CORS, Redis, interceptors, TracingConfig)
├── controller/     # REST controllers (AuthController, UserController)
├── dto/            # Request/Response DTOs with Jakarta Validation
├── entity/         # MyBatis entities
├── exception/      # BusinessException + GlobalExceptionHandler
├── interceptor/    # AuthInterceptor, RequestLogInterceptor (OTel tracing)
├── mapper/         # MyBatis mapper interfaces
├── monitor/        # Actuator health indicators
├── security/       # UserContext (holds current user from JWT)
├── service/        # Service layer (AuthService, UserService)
└── util/           # JwtUtil
```

### Frontend Structure
```
ui/src/
├── api/index.js        # Axios instance with auto JWT injection
├── stores/user.js      # Pinia store for token/userId persistence
├── router/index.js     # Routes + auth guard (redirect to /login)
├── views/
│   ├── Login.vue       # Login page
│   ├── Register.vue    # Register page
│   ├── UserList.vue    # User CRUD table with pagination
│   └── UserEdit.vue    # Edit user details
└── components/
    └── Navbar.vue      # Header with logout
```

### Configuration
Multi-environment config:
- `application.yml` - Base config with environment variable placeholders
- `application-dev.yml` - Development (active by default)
- `application-prod.yml` - Production

JWT expiration: 24 hours (`jwt.expiration: 86400000`)

### Important Patterns

**Authentication**: JWT token required for all `/api/users/*` endpoints. Token in `Authorization: Bearer <token>` header. Login via `/api/auth/login`.

**Rate Limiting**: `@RateLimit(key="xxx", time=60, count=10, limitType=LimitType.IP)` on controller methods. Uses Redis + Lua sliding window algorithm. Types: `DEFAULT` (global), `IP`, `USER`.

**Global Exception Handling**: `GlobalExceptionHandler` catches `BusinessException`, `MethodArgumentNotValidException`, `ConstraintViolationException`.

**API Response**: All responses use `Result<T>` wrapper. Pagination uses `PageResult<T>`.

**Request Logging**: `RequestLogInterceptor` logs all requests with OpenTelemetry traceId/spanId (MDC). Log format: `[traceId:%X{traceId:-}] [spanId:%X{spanId:-}]`. Slow requests (>3s) warn.

**Distributed Tracing**: OpenTelemetry + Micrometer Tracing auto-generates traceId for all HTTP requests. Configure via `management.tracing.*` in application.yml. Export to Jaeger via OTLP (port 4317) when `OTEL_EXPORT_ENABLED=true`.

### Database
- MySQL database `jguard`
- Schema: `src/main/resources/schema.sql` (includes BCrypt hashed test passwords)
- MyBatis mappers: `src/main/resources/mapper/*.xml`
- All queries use `#{}` precompiled parameters (SQL injection safe)

### Monitoring
- `/actuator/health` - Health status (DB + memory)
- `/actuator/metrics` - Performance metrics
- `/swagger-ui.html` - API documentation

### Distributed Tracing (OpenTelemetry)
- **traceId/spanId**: Auto-injected into MDC for all HTTP requests
- **Log pattern**: `[traceId:%X{traceId:-}] [spanId:%X{spanId:-}]`
- **Backend**: Jaeger (optional, via OTLP port 4317)
- **Config**: `management.tracing.sampling.probability` (default 100% dev, 10% prod)
- **Enable export**: Set `OTEL_EXPORT_ENABLED=true` and `OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger:4317`