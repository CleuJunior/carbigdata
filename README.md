# carbigdata

REST API for managing clients and their occurrences (incidents/complaints). Built with Spring Boot 4 and secured with JWT.

## Tech Stack

- **Java 17** + **Spring Boot 4.0.5**
- **PostgreSQL 16** — production database
- **Flyway** — database migrations and seed data
- **Spring Security** + **JJWT 0.12.3** — stateless JWT authentication
- **Testcontainers** — real PostgreSQL container for integration tests

## Prerequisites

- Java 17+
- Docker (for PostgreSQL and integration tests)

## Running

Start the database:

```bash
docker-compose up -d
```

Run the application:

```bash
./gradlew bootRun
```

The API will be available at `http://localhost:8080`.

Interactive API documentation (Swagger UI) is available at `http://localhost:8080/swagger-ui.html`.

## Authentication

All endpoints except `/api/v1/auth/login` require a Bearer token.

**Login:**

```
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```

Returns `{ "token": "<jwt>" }`. Pass it on subsequent requests:

```
Authorization: Bearer <jwt>
```

> The `admin/admin` user is hardcoded in `SecurityConfig` and intended to be replaced.

## API Endpoints

| Method | Path | Role required |
|--------|------|---------------|
| `POST` | `/api/v1/auth/login` | — |
| `GET` | `/api/v1/clients/{id}` | ADMIN, USER |
| `GET` | `/api/v1/clients` | ADMIN, USER |
| `GET` | `/api/v1/clients/pageable?page=0&size=15` | ADMIN, USER |
| `POST` | `/api/v1/clients` | ADMIN |
| `PUT` | `/api/v1/clients/{id}` | ADMIN |
| `DELETE` | `/api/v1/clients/{id}` | ADMIN |

**Client request body:**

```json
{
  "name": "João Silva",
  "birthdate": "1990-05-20",
  "cpf": "123.456.789-09"
}
```

## Domain Model

```
Client
  └── Occurrence  (status: ACTIVE | FINISHED)
        ├── Address
        └── PhotoOccurrence
```

All entities use UUIDs as primary keys, carry audit timestamps (`creationDate`, `updateDate`), and are soft-deleted (never physically removed from the database).

## Running Tests

Docker must be running for integration tests — Testcontainers spins up a PostgreSQL 16 container automatically.

```bash
# Unit tests (no Docker required)
./gradlew test

# Integration tests (Docker required)
./gradlew integrationTest

# Single test class or method
./gradlew test --tests br.com.ctkd.service.ClientServiceTest
./gradlew test --tests "br.com.ctkd.service.ClientServiceTest.shouldGetClientById"
./gradlew integrationTest --tests br.com.ctkd.repository.ClientRepositoryIT
./gradlew integrationTest --tests "br.com.ctkd.repository.ClientRepositoryIT.shouldFindByIdAndNotDeleted"
```

### Test configuration

Integration tests use `api/src/test/resources/application.yml`, which overrides the main config to point at the Testcontainers database and run Flyway migrations (including seed data). No profile flag is needed — the file is automatically picked up from the test classpath.

Repository tests (`@DataJpaTest`) and controller tests (`@SpringBootTest`) both run against a real PostgreSQL instance. Each test starts with a clean slate: seed data inserted by Flyway migrations is wiped before every test method via `@Sql`, and for `@DataJpaTest` those deletes are rolled back after each test so seed data is restored for the next one.
