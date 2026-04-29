# carbigdata

REST API for managing clients and their occurrences (incidents/complaints). Built with Spring Boot 4 and secured with JWT.

## Tech Stack

- **Java 17** + **Spring Boot 4.0.5**
- **PostgreSQL 16** — production database
- **Min.io** — object storage for photo evidence
- **Flyway** — database migrations and seed data
- **Spring Security** + **JJWT 0.12.3** — stateless JWT authentication (30-minute expiration)
- **Testcontainers** — real PostgreSQL container for integration tests
- **Docker Compose** — orchestrates all containers (PostgreSQL, Min.io, application)

## Prerequisites

- Java 17+
- Docker

## Running with Docker Compose (recommended)

Start all containers (PostgreSQL, Min.io, and the application):

```bash
docker-compose up -d --build
```

The API will be available at `http://localhost:8080`.

> On first run, the `carbigdata-occurrences` container waits for PostgreSQL and Min.io to be healthy before starting.

Before uploading photos, create the `occurrences` bucket in Min.io:

1. Open the Min.io console at `http://localhost:9001` (user: `minioadmin`, password: `minioadmin123`)
2. Create a bucket named `occurrences`

## Running locally (development)

Start only the dependencies:

```bash
docker-compose up -d postgres minio
```

Run the application:

```bash
./gradlew bootRun
```

The API will be available at `http://localhost:8080`.

## Swagger UI

Interactive API documentation is available at `http://localhost:8080/swagger-ui.html`.

## Authentication

All endpoints except `/api/v1/auth/login` require a Bearer token. Tokens expire after 30 minutes.

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

> The `admin/admin` user is hardcoded in `SecurityConfig` and intended to be replaced with a real user store.

## API Endpoints

### Auth

| Method | Path | Role |
|--------|------|------|
| `POST` | `/api/v1/auth/login` | — |

### Clients

| Method | Path | Role |
|--------|------|------|
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

### Addresses

| Method | Path | Role |
|--------|------|------|
| `GET` | `/api/v1/address/{id}` | ADMIN, USER |
| `GET` | `/api/v1/address` | ADMIN, USER |
| `GET` | `/api/v1/address/pageable?page=0&size=15` | ADMIN, USER |
| `POST` | `/api/v1/address` | ADMIN |
| `PUT` | `/api/v1/address/{id}` | ADMIN |
| `DELETE` | `/api/v1/address/{id}` | ADMIN |

**Address request body:**

```json
{
  "streetName": "Rua das Flores",
  "neighborhood": "Centro",
  "zipCode": "01001-000",
  "city": "São Paulo",
  "state": "SP"
}
```

### Occurrences

| Method | Path | Role |
|--------|------|------|
| `GET` | `/api/v1/occurrences/{id}` | ADMIN, USER |
| `GET` | `/api/v1/occurrences` | ADMIN, USER |
| `GET` | `/api/v1/occurrences/pageable?page=0&size=15` | ADMIN, USER |
| `POST` | `/api/v1/occurrences` | ADMIN |
| `POST` | `/api/v1/occurrences/search` | ADMIN, USER |
| `PATCH` | `/api/v1/occurrences/{id}` | ADMIN |

**Create occurrence** — `multipart/form-data`:
- `data` (JSON part): occurrence metadata
- `photos` (file part, optional): one or more image files uploaded to Min.io

```json
{
  "clientId": "<uuid>",
  "addressId": "<uuid>",
  "occurrenceDate": "2024-03-10"
}
```

**Search occurrences** — all fields optional, returns paginated results:

```json
{
  "clientName": "João",
  "cpf": "123.456.789-09",
  "occurrenceDate": "2024-03-10",
  "city": "São Paulo",
  "sortBy": "occurrenceDate",
  "sortDirection": "DESC",
  "page": 0,
  "size": 15
}
```

`sortBy` accepts `occurrenceDate` (default) or `city`. `sortDirection` accepts `ASC` or `DESC` (default `DESC`).

**Close occurrence** — `PATCH /api/v1/occurrences/{id}` transitions status from `ACTIVE` to `FINISHED`. Once finished, the occurrence cannot be changed again (returns HTTP 409).

**Occurrence response** — includes nested `client`, `address`, and `photos`. Each photo contains a presigned `url` for direct download from Min.io, valid for 1 hour:

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "occurrenceDate": "2024-03-10",
  "status": "ACTIVE",
  "client": { "id": "...", "name": "João Silva", "cpf": "123.456.789-09" },
  "address": { "id": "...", "streetName": "Rua das Flores", "city": "São Paulo" },
  "photos": [
    {
      "id": "...",
      "pathBucket": "photos/uuid.jpg",
      "url": "http://localhost:9000/occurrences/photos/uuid.jpg?X-Amz-Signature=...",
      "hash": "sha256..."
    }
  ],
  "creationDate": "2024-03-10T14:00:00Z"
}
```

### Photos

| Method | Path | Role |
|--------|------|------|
| `GET` | `/api/v1/photos/occurrences/{occurrenceId}` | ADMIN, USER |
| `GET` | `/api/v1/photos/{photoId}` | ADMIN, USER |
| `DELETE` | `/api/v1/photos/{photoId}` | ADMIN |

## Domain Model

```
Client
  └── Occurrence  (status: ACTIVE | FINISHED)
        ├── Address
        └── PhotoOccurrence  (stored in Min.io; response includes presigned URL)
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
./gradlew test --tests "br.com.ctkd.service.OccurrenceServiceTest.shouldCloseOccurrence"
./gradlew integrationTest --tests br.com.ctkd.repository.ClientRepositoryIT
./gradlew integrationTest --tests "br.com.ctkd.repository.ClientRepositoryIT.shouldFindByIdAndNotDeleted"
```

## What was implemented

All requirements from the technical assessment were implemented:

- JWT authentication with 30-minute token expiration
- CRUD for Client, Address, and Occurrence
- Occurrence registration with photo upload (multipart) to Min.io
- Occurrence listing with nested client/address data and presigned photo URLs
- Filterable, sortable, and paginated occurrence search (by name, CPF, date, city)
- Occurrence finalization endpoint (`PATCH`) — prevents re-closing a finished occurrence
- Pagination available on all list queries (`/pageable` endpoints + search)
- Flyway migrations for schema and seed data
- Docker Compose orchestrating all three containers (PostgreSQL, Min.io, application)
- Swagger/OpenAPI documentation
- Unit tests (service and factory layers) and integration tests (Testcontainers)
