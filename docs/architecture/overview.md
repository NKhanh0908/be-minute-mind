# System Architecture Overview

## High-Level System Architecture

This flowchart outlines the high-level infrastructure layout, indicating data transmission boundaries between the Frontend SPA, the Spring Boot API, and external third-party services.

```mermaid
flowchart TD
    Client[Frontend: Vite + React SPA] -->|HTTPS Requests / JSON payloads| API[Spring Boot REST API: Port 8080]
    
    API -->|1. Persistent Storage| DB[(PostgreSQL DB: Supabase)]
    API -->|2. Caching & Caching Locks| Redis[(Redis Store)]
    API -->|3. Image Uploads| Cloudinary[Cloudinary Cloud Storage]
    
    subgraph Redis Cache Details
        Redis -->|Enforces Single Session| Lock[Active Session Token vilo:active_session:userId]
        Redis -->|Enforces IP Rate Limits| Bucket[Bucket4j Lettuce Proxy manager]
    end
    
    subgraph Spring Boot Modules
        API --> Filter[JwtAuthenticationFilter]
        API --> Aspect[RateLimitAspect: Bucket4j AOP]
    end
```

---

## Package Architecture

This flowchart maps the layout of the Spring Boot package structure, showcasing package dependencies and the data-flow path.

```
[com.be.minutemind]
  ├── annotation (Custom Annotations & Resolvers)
  ├── aspect (AOP Aspects - Rate Limit)
  ├── config (SecurityConfig, WebMvcConfig, Bucket4jConfig)
  ├── controller (REST Entrypoints)
  ├── dtos (request & response records)
  ├── entities (JPA Database Entities)
  ├── enums (GoalStatus, TaskStatus, etc.)
  ├── exception (Global Error Handling)
  ├── filter (Jwt Filter)
  ├── helper (JwtHelper, CloudinaryService)
  ├── mapper (MapStruct Model Mappers)
  ├── repository (Spring Data JPA Repositories)
  └── service (Service Interfaces)
        └── serviceImpl (Workflow Implementations)
```

```mermaid
flowchart TD
    Filter[filter: JwtAuthenticationFilter] -->|Validates & Binds Principal| Controller[controller: REST Controllers]
    Aspect[aspect: RateLimitAspect] -->|Intercepts & Throws 429| Controller
    
    Controller -->|Binds payload to| DTOs[dtos: Request / Response Records]
    Controller -->|Invokes interface| Service[service: Service Interfaces]
    
    ServiceImpl[service.serviceImpl: Implementations] -.->|Implements| Service
    
    ServiceImpl -->|Invokes| Repositories[repository: JPA Repositories]
    ServiceImpl -->|Converts with| Mappers[mapper: MapStruct Mappers]
    ServiceImpl -->|Uses utility| Helpers[helper: JwtHelper, CloudinaryService]
    
    Repositories -->|Persists| Entities[entities: JPA Entities]
    Repositories -->|Queries| DB[(PostgreSQL DB)]
```

---

## Request Lifecycle

This sequence diagram illustrates the lifecycle of a typical client request (e.g. creating a goal: `POST /goals`) as it passes through the security filters, rate-limiting aspects, controllers, services, repositories, and the database.

```mermaid
sequenceDiagram
    autonumber
    participant Client
    participant Filter as JwtAuthenticationFilter
    participant Aspect as RateLimitAspect
    participant Controller as GoalController
    participant Service as GoalServiceImpl
    participant Mapper as GoalMapper
    participant Repo as GoalRepository
    participant DB as PostgreSQL DB

    Client->>Filter: POST /goals (Header: Bearer <token>)
    activate Filter
    Note over Filter: Extract userId from Token
    Filter->>Filter: Validate User is Active in DB
    Filter-->>Aspect: Request Authorized (Set Authentication Context)
    deactivate Filter
    
    activate Aspect
    Note over Aspect: Check Redis Token Bucket for IP
    Aspect->>Aspect: Token Consumed (Within Rate Limit)
    Aspect-->>Controller: Proceed to endpoint method
    deactivate Aspect
    
    activate Controller
    Note over Controller: Bind Request Body & Validate @Valid
    Controller->>Service: createGoal(userId, GoalRequest)
    activate Service
    
    Service->>Mapper: toEntity(GoalRequest)
    activate Mapper
    Mapper-->>Service: Goal Entity (transient)
    deactivate Mapper
    
    Service->>Repo: save(Goal)
    activate Repo
    Repo->>DB: INSERT INTO goals ...
    activate DB
    DB-->>Repo: Insert Successful (id=1, status=ACTIVE)
    deactivate DB
    Repo-->>Service: Goal Entity (managed)
    deactivate Repo
    
    Service->>Mapper: toResponse(Goal, loggedMinutes=0)
    activate Mapper
    Mapper-->>Service: GoalResponse DTO
    deactivate Mapper
    
    Service-->>Controller: GoalResponse DTO
    deactivate Service
    Controller-->>Client: 201 Created (ApiResponse<GoalResponse>)
    deactivate Controller
```

---

## Core Framework & Layer Responsibilities

### 1. Presentation / Controller Layer
- Endpoint routing and CORS checking.
- Maps JSON payloads into Java records (`dtos`).
- Performs input validation (e.g. `@NotBlank`, `@Size`).
- Intercepts requests for custom arguments binding (e.g. `@CurrentUser Long userId`).

### 2. Service / Business Logic Layer
- Orchestrates transaction boundaries using `@Transactional`.
- Evaluates domain-specific permissions (e.g., verifying a user owns a task before updating it).
- Implements calculations, streak checks, and badge awarding logic.

### 3. Data Access / Repository Layer
- Integrates JpaRepository for standard CRUD functions.
- Implements custom query statements.

### 4. Infrastructure Layer
- **Distributed Rate Limiting**: Managed by `Bucket4jConfig` and `RateLimitAspect` leveraging Lettuce Redis connection managers.
- **Stateless Authentication**: Filters JWT Bearer tokens, resolves UserDetails, and handles specific expired/invalid token payloads.
- **Third-Party Storage**: Uploads, deletes, and updates avatars via Cloudinary.
