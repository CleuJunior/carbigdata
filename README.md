# carbigdata

REST API for managing clients and their occurrences (incidents/complaints). Built with Spring Boot 4 and secured with JWT.

## Tech Stack

- **Java 17** + **Spring Boot 4.0.5**
- **PostgreSQL 16** — production database
- **Flyway** — database migrations
- **Spring Security** + **JJWT 0.12.3** — stateless JWT authentication
- **H2** — in-memory database for tests

## Prerequisites

- Java 17+
- Docker (for PostgreSQL)

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

```bash
# Unit tests (no Docker required)
./gradlew test

# Integration tests (no Docker required — uses H2)
./gradlew integrationTest

# Single test class or method
./gradlew test --tests br.com.ctkd.service.ClientServiceTest
./gradlew test --tests "br.com.ctkd.service.ClientServiceTest.shouldGetClientById"
```
