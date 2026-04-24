# Identity Service API (API-First, Contract-Driven)

![CI](https://github.com/DanieleMasone/identity-service/actions/workflows/ci.yml/badge.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

## Overview

This project is a production-style **Identity / User Service** built using an **API-first, contract-driven approach**.

The goal of this repository is to demonstrate how to design and implement APIs that are:

* Consistent
* Versioned
* Well-documented
* Consumer-friendly

---

## Key Features

* **OpenAPI-first design** (YAML contract is the source of truth)
* **Code generation** using OpenAPI Generator
* **MapStruct-based DTO mapping (no manual mapping)**
* **Versioned APIs** (`/api/v1`, `/api/v2`)
* **RFC7807-compliant error handling**
* **Strict separation: API / Domain / Service layers**
* **Bean validation (Jakarta Validation)**
* **Unit & integration testing (Testcontainers)**
* **Mock server (Prism)**
* **Contract testing ready**

---

## Tech Stack

* Java 21
* Spring Boot 4.0.6
* Maven
* OpenAPI 3
* MapStruct
* PostgreSQL
* Testcontainers
* Prism (mock server)

---

## API Design Approach

The API contract is defined first in:

```
src/main/resources/openapi/identity-api.yaml
```

From this contract:

* Server interfaces are generated using OpenAPI Generator
* DTOs are automatically derived from the specification
* Controllers implement generated interfaces
* MapStruct handles DTO ↔ Domain mapping

This ensures:

* No contract drift
* Strong typing between layers
* Reduced boilerplate code
* Clear separation between API and business logic

---

## Architecture Highlights

This project follows a layered architecture with clear separation of concerns:

* **API Layer**
    - OpenAPI-generated interfaces
    - Controllers only handle HTTP orchestration

* **Service Layer**
    - Business logic
    - Versioned services (`V1`, `V2`) for API evolution

* **Domain Layer**
    - JPA entities
    - Repository interfaces

* **Mapping Layer**
    - MapStruct used for automatic DTO ↔ Entity mapping
    - Eliminates manual mapping boilerplate
    - Ensures consistency between API versions

---

## Design Decisions

* Manual mapping was intentionally avoided in favor of MapStruct to reduce boilerplate and enforce consistency.
* API versioning is handled at URL level (`/v1`, `/v2`) instead of header-based versioning for simplicity and clarity.
* DTOs are separated per API version to allow controlled evolution without breaking existing clients.

---

## Tooling

* OpenAPI Generator → API interface + models
* MapStruct → DTO mapping layer
* Prism → OpenAPI mock server for contract testing

---

## Running the Application

```bash
mvn clean install
mvn spring-boot:run
```

---

## Running Tests

```bash
mvn test
```

Integration tests use **Testcontainers**, so Docker must be running.

---

## Mock Server

You can run a mock version of the API without starting the application:

```bash
prism mock src/main/resources/openapi/identity-api.yaml
```

---

## API Versioning

* `v1` → stable API
* `v2` → backward-compatible improvements

---

## Error Handling

Errors follow the **RFC7807 Problem Details** standard.

Example:

```json
{
  "type": "about:blank",
  "title": "Validation error",
  "status": 400,
  "detail": "Email is invalid"
}
```

---

## Why This Project

This repository demonstrates:

* API design skills
* Contract-first development
* Backend architecture best practices
* Production-ready patterns

---

## Future Improvements

* Authentication (OAuth2 / JWT)
* Rate limiting
* Audit logging
* Event-driven integration (Kafka)

---
