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
```

## Architecture

Spring Boot 3.2.0 + Java 17 enterprise REST API with MyBatis + PageHelper.

### Key Tech Stack
- **Spring Boot 3.x** - Uses Jakarta EE 9+ namespace (`jakarta.*` instead of `javax.*`)
- **MyBatis 3.x** - XML mapper files in `src/main/resources/mapper/`
- **PageHelper** - Pagination plugin, `PageHelper.startPage()` must be called immediately before query
- **Springdoc OpenAPI 2.x** - Swagger UI at `/swagger-ui.html`

### Package Structure
```
com.example.demo/
├── annotation/     # Custom annotations (e.g., @RateLimit)
├── aspect/         # AOP aspects (performance, logging, rate limiting)
├── common/         # Shared classes (Result wrapper)
├── config/         # Spring configurations (CORS, interceptors, Swagger)
├── controller/     # REST controllers
├── dto/            # Request/Response DTOs with validation
├── entity/         # JPA/MyBatis entities
├── exception/      # BusinessException + GlobalExceptionHandler
├── interceptor/    # Spring MVC interceptors (auth, request logging)
├── mapper/         # MyBatis mapper interfaces
├── monitor/        # Actuator health indicators
└── service/        # Service layer interfaces and implementations
```

### Important Patterns

**Global Exception Handling**: `GlobalExceptionHandler` uses `@RestControllerAdvice` to catch:
- `BusinessException` - Custom business errors
- `MethodArgumentNotValidException` - `@Valid` DTO validation failures
- `ConstraintViolationException` - `@RequestParam` validation failures

**Rate Limiting**: Use `@RateLimit(key="xxx", time=60, count=10)` annotation on controller methods. Current implementation is memory-based; production should use Redis.

**Request Logging**: `RequestLogInterceptor` logs all requests with duration. Slow requests (>3s) trigger warnings.

**API Response**: All responses use `Result<T>` wrapper with `code`, `message`, `data` fields.

**Pagination**: `PageResult<T>` wrapper for paginated data. Query params extend `PageRequest` (pageNum, pageSize).

### Database
- MySQL on localhost:3306/database `demo`
- Schema: `src/main/resources/schema.sql`
- MyBatis mappers: `src/main/resources/mapper/*.xml`

### Monitoring Endpoints
- `/actuator/health` - Health status (DB + memory checks)
- `/actuator/metrics` - Performance metrics
- `/swagger-ui.html` - API documentation