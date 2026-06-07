# System Architecture Overview

This document describes the high-level technical architecture, design patterns, and cross-cutting infrastructure integrations of the **MinuteMind** backend.

---

## Architectural Pattern: Layered Architecture

The backend follows the classic **Layered (n-tier) Architecture** to ensure clean separation of concerns, maintainability, and testability.

```
[ Client / Web Browser ]
          │
          ▼
┌────────────────────────────────────────────────────────┐
│  Presentation / Controller Layer                       │
│  - Endpoint definitions, API routing, CORS             │
│  - Validation constraints (@Valid, @NotNull, etc.)      │
│  - Rate Limiting Aspect enforcement                    │
└─────────────────────────┬──────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────┐
│  Service / Business Logic Layer                        │
│  - Interfaces defining domain workflows                │
│  - Implementations containing actual business rules    │
│  - Transaction boundaries (@Transactional)            │
└─────────────────────────┬──────────────────────────────┘
                          │
                          ▼
┌────────────────────────────────────────────────────────┐
│  Data Access / Repository Layer                        │
│  - Spring Data JPA Repositories                        │
│  - Entity mappings and database indexes                │
└─────────────────────────┬──────────────────────────────┘
                          │
                          ▼
             ┌────────────┴────────────┐
             │                         │
             ▼                         ▼
      [ PostgreSQL DB ]            [ Redis ]
```

### 1. Controller Layer (`com.be.minutemind.controller`)
- Defines REST endpoints using Spring Web annotations (`@RestController`, `@GetMapping`, etc.).
- Integrates with Spring Security to authenticate incoming requests via a custom JWT filter.
- Applies input validation constraints (using `jakarta.validation` annotations) to reject invalid payloads early.
- Integrates with Swagger/OpenAPI annotations to generate interactive API documentation.

### 2. Service Layer (`com.be.minutemind.service` & `serviceImpl`)
- Uses the **Interface-Implementation** pattern. This separates the API contracts from their specific execution rules, facilitating unit testing and mock injection.
- Manages transactional boundaries. Methods modifying state are annotated with `@Transactional` (using default rollback rules for `RuntimeException`). Read-only methods are configured with `@Transactional(readOnly = true)` to optimize database performance.

### 3. Repository Layer (`com.be.minutemind.repository`)
- Extends `JpaRepository` to provide standardized CRUD capabilities.
- Implements custom native queries where standard JPQL is insufficient (e.g., aggregation queries for streaks and focus statistics).

---

## Cross-Cutting Infrastructures

### 1. Redis Integration
Redis plays two distinct, critical roles in the architecture:
- **Active Session Cache**: Tracked under `vilo:active_session:<userId>`, it serves as a fast-lookup mechanism to verify and manage active focus timers. It acts as a distributed lock, preventing concurrent timer execution.
- **Distributed Rate Limiting Manager**: Configured via Lettuce (`LettuceBasedProxyManager` in `Bucket4jConfig`), it stores rate-limiting bucket tokens in Redis. This ensures that rate limits are enforced consistently across multiple running backend instances (stateless scaling).

### 2. Custom AOP Rate Limiting
- Built using **Aspect-Oriented Programming (AOP)** (`@Aspect` in `RateLimitAspect.java`).
- Leverages the **Bucket4j** library for token bucket rate-limiting algorithms.
- Custom annotation `@RateLimit(requests, perSeconds)` can be placed on any controller method.
- Enforces limits based on **IP Address** and method signatures:
  - Default limits for authentication endpoints (`/register`, `/login`, `/refresh`): **10 requests per 60 seconds**.
  - If a client exceeds the limit, the aspect intercepts the execution and throws a `429 Too Many Requests` status code.

### 3. Image Uploads (Cloudinary Integration)
- Managed via `CloudinaryService.java` wrapping the official `cloudinary-http44` client.
- Handles profile picture uploads for users.
- When a user uploads a new avatar:
  - The service uploads the file to the Cloudinary cloud storage under folder `minute-mind/avatars`.
  - It extracts the `publicId` from the old avatar's URL and deletes the old file from Cloudinary storage to prevent storage leaks.
  - Updates the new secure HTTPS URL to the database.
