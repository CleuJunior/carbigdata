# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

All commands run from the project root using the Gradle wrapper. The working module is `api/`.

```bash
# Build
./gradlew build

# Run the application
./gradlew bootRun

# Unit tests
./gradlew test

# Integration tests
./gradlew integrationTest

# Single unit test class or method
./gradlew test --tests br.com.ctkd.service.ClientServiceTest
./gradlew test --tests "br.com.ctkd.service.ClientServiceTest.shouldGetClientById"

# Single integration test class or method
./gradlew integrationTest --tests br.com.ctkd.repository.ClientRepositoryIT
./gradlew integrationTest --tests "br.com.ctkd.repository.ClientRepositoryIT.shouldFindByIdAndNotDeleted"
```

## Environment Setup

Start the PostgreSQL database before running the application:

```bash
docker-compose up -d
```

This brings up PostgreSQL 16 on port 5432 with database `carbigdata`, user `postgres`, password `postgres`. Flyway migrations run automatically on startup.

For tests, no Docker is needed — they use an H2 in-memory database (configured in `application-test.yml`).

## Architecture

Spring Boot 4.0.5 REST API. The domain models clients and their occurrences (incidents/complaints):

```
Client → (has many) → Occurrence → (has many) → PhotoOccurrence
                         ↓
                      Address
```

**Layers** (package `br.com.ctkd`):

- `controller/` — REST endpoints (`/api/v1/clients`, `/api/v1/auth`)
- `service/` — Business logic
- `repository/` — Spring Data JPA repositories with custom queries
- `domain/` — JPA entities; all extend `BaseEntity` (UUID pk, audit timestamps, soft-delete `deleted` flag). Enums: `Role` (`ADMIN`, `USER`), `StatusOccurrence` (`ACTIVE`, `FINISHED`)
- `dto/` — Request/response DTOs (`request/`, `reponse/` — note the typo in the package name)
- `factory/` — Manual DTO↔entity mapping (no MapStruct)
- `exceptions/` — `NotFoundException` and `ErrorExceptionHandler` (global `@RestControllerAdvice`)
- `i18n/` — `LocalizedMessageTranslationService` wraps Spring `MessageSource`; messages defined in `resources/i18n/messages_en.properties`
- `config/auth/` — `JwtService` + `JwtAuthFilter` for stateless JWT authentication

**API endpoints:**

| Method | Path | Roles |
|--------|------|-------|
| `POST` | `/api/v1/auth/login` | public |
| `GET` | `/api/v1/clients/{id}` | ADMIN, USER |
| `GET` | `/api/v1/clients` | ADMIN, USER |
| `GET` | `/api/v1/clients/pageable` | ADMIN, USER |
| `POST` | `/api/v1/clients` | ADMIN |
| `PUT` | `/api/v1/clients/{id}` | ADMIN |
| `DELETE` | `/api/v1/clients/{id}` | ADMIN |

Role checks are enforced via `@PreAuthorize` on controller methods. The only user currently defined is an in-memory account: username `admin`, password `admin`, role `ADMIN` (declared in `SecurityConfig` — intended to be replaced).

**Key cross-cutting patterns:**

- **Soft deletes**: entities are never physically deleted; queries filter by `deleted = false`.
- **JPA Auditing**: `@CreatedDate` / `@LastModifiedDate` populated via `JpaAuditingLogConfig`.
- **Flyway migrations**: DDL is managed exclusively by Flyway (`hibernate.ddl-auto=none`); migration files live in `resources/db/migration/`.
- **Internationalized errors**: exception messages are resolved through `LocalizedMessageTranslationService` so all user-facing strings come from property files.
- **Security**: Spring Security with JWT; session is stateless. `SecurityConfig` defines the filter chain. JWT is configured via `security.jwt.secret` and `security.jwt.expiration` in `application.yml` (mapped to `JwtProperties`).

## Tech Stack

- **Java 17**, **Spring Boot 4.0.5**, **Gradle 9.4.1**
- **PostgreSQL 16** (production) / **H2** (tests)
- **Flyway** for migrations, **Lombok** for boilerplate reduction
- **JJWT 0.12.3** for JWT handling
- **JUnit 5 + Mockito** (unit), **`@DataJpaTest`** (integration)
- CPF validation via Hibernate Validator `@CPF`
