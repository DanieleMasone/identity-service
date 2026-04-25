# Identity Service API

![CI](https://github.com/DanieleMasone/identity-service/actions/workflows/ci.yml/badge.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

Production-style backend service for user identity management, built with an API-first and contract-driven approach.

The repository is designed to showcase backend engineering practices that matter in a real team: OpenAPI contracts, generated API interfaces, layered architecture, persistence migrations, validation, RFC 7807 errors, unit tests, integration tests, Docker support, and CI.

## What It Demonstrates

* API-first design with OpenAPI as the source of truth
* Generated Spring MVC interfaces and models from the OpenAPI contract
* Versioned APIs: `/api/v1` and `/api/v2`
* Layered structure: API, service, domain, persistence, infrastructure
* MapStruct mapping between generated API models and domain entities
* PostgreSQL persistence with Flyway migrations
* BCrypt password hashing
* RFC 7807 `ProblemDetail` error responses
* Fast unit tests with Mockito
* Integration tests with Testcontainers
* GitHub Actions CI

## Tech Stack

* Java 21
* Spring Boot 4
* Spring MVC
* Spring Data JPA
* PostgreSQL
* Flyway
* OpenAPI Generator
* MapStruct
* Testcontainers
* Maven
* Docker Compose

## API Contract

The contract lives in:

```text
src/main/resources/openapi/identity-api.yaml
```

During the Maven build, OpenAPI Generator creates:

* `UsersV1Api`
* `UsersV2Api`
* request and response models

Controllers implement the generated interfaces, so the HTTP layer stays aligned with the contract.

## Generated Sources

Generated code is created by Maven during the build and stored under `src/generated/` so the generated API layer is visible in the repository.

Generate OpenAPI interfaces/models and the MapStruct implementation:

```bash
mvn clean compile
```

Generated output:

```text
src/generated/java/com/dmasone/identity/api/generated
src/generated/annotations/com/dmasone/identity/api/mapper/UserMapperImpl.java
```

Generated files should not be edited manually. They are reproducible from the OpenAPI contract and the MapStruct mapper interface.

## Endpoints

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/v1/users` | Create a v1 user |
| `GET` | `/api/v1/users/{id}` | Get a v1 user |
| `DELETE` | `/api/v1/users/{id}` | Soft-delete a user |
| `POST` | `/api/v2/users` | Create a v2 user with profile fields |
| `GET` | `/api/v2/users/{id}` | Get a v2 user |
| `PATCH` | `/api/v2/users/{id}` | Partially update profile/status |

Swagger UI is available at:

```text
http://localhost:8080/api/swagger-ui.html
```

## Run Locally

Start PostgreSQL:

```bash
docker compose up -d db
```

Run the service:

```bash
mvn spring-boot:run
```

Default database settings:

```text
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

## Tests

Run unit tests:

```bash
mvn test
```

Run the full verification, including Testcontainers integration tests:

```bash
mvn verify
```

Docker must be running for integration tests.

## Build The Docker Image

Package the application:

```bash
mvn clean package
```

Build the image:

```bash
docker build -t identity-service .
```

## Design Notes

* v1 keeps the smallest stable user contract.
* v2 extends the API with profile fields while preserving v1.
* Deletes are soft deletes through the `INACTIVE` status.
* API models are generated; domain entities remain internal.
* MapStruct is configured to fail on unmapped target properties, making DTO drift visible during compilation.

## Future Improvements

* OAuth2/JWT authentication
* Role and permission model
* Audit trail
* Rate limiting
* Event publishing for user lifecycle changes
