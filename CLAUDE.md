# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

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
mysql -u root -proot demo < src/main/resources/schema.sql
```

## Architecture

Spring Boot 3.2.0 + Java 17 enterprise REST API with MyBatis + PageHelper + JWT Auth + Redis Rate Limiting.

### Key Tech Stack
- **Spring Boot 3.x** - Uses Jakarta EE 9+ namespace (`jakarta.*` instead of `javax.*`)
- **MyBatis 3.x** - XML mapper files in `src/main/resources/mapper/`
- **PageHelper** - Pagination plugin, `PageHelper.startPage()` must be called immediately before query
- **JWT (jjwt)** - Token-based authentication with BCrypt password hashing
- **Redis** - Distributed rate limiting with Lua scripts (graceful degradation when unavailable)
- **Springdoc OpenAPI 2.x** - Swagger UI at `/swagger-ui.html`

### Package Structure
```
com.example.demo/
├── annotation/     # Custom annotations (@RateLimit)
├── aspect/         # AOP aspects (performance, rate limiting)
├── common/         # Result wrapper
├── config/         # Spring configs (CORS, Redis, interceptors)
├── controller/     # REST controllers (AuthController, UserController)
├── dto/            # Request/Response DTOs with Jakarta Validation
├── entity/         # MyBatis entities
├── exception/      # BusinessException + GlobalExceptionHandler
├── interceptor/    # AuthInterceptor, RequestLogInterceptor
├── mapper/         # MyBatis mapper interfaces
├── monitor/        # Actuator health indicators
├── security/       # UserContext (holds current user from JWT)
├── service/        # Service layer (AuthService, UserService)
└── util/           # JwtUtil
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

**Request Logging**: `RequestLogInterceptor` logs all requests with traceId (MDC) and duration. Slow requests (>3s) warn.

### Database
- MySQL database `demo`
- Schema: `src/main/resources/schema.sql` (includes BCrypt hashed test passwords)
- MyBatis mappers: `src/main/resources/mapper/*.xml`
- All queries use `#{}` precompiled parameters (SQL injection safe)

### Monitoring
- `/actuator/health` - Health status (DB + memory)
- `/actuator/metrics` - Performance metrics
- `/swagger-ui.html` - API documentation