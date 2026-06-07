# Security & Authentication Architecture

MinuteMind implements a stateless, token-based authentication system using Spring Security, JSON Web Tokens (JWT), and a Refresh Token Rotation (RTR) mechanism.

---

## Configuration & Filters

### 1. Spring Security Config (`SecurityConfig.java`)
- **Session Policy**: Set to `SessionCreationPolicy.STATELESS` (no HTTP sessions are created or maintained by the server).
- **CSRF**: Disabled (safe because no cookies are used for authentication).
- **CORS**: Delegated to `WebMvcConfig.java` to fetch allowed origins dynamically from properties (`app.cors.allowed-origins`).
- **Authorization Rules**:
  - **Permitted**: `/auth/**` (register, login, refresh), `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**` (OpenAPI docs), `/actuator/health` (monitoring).
  - **Authenticated**: All other requests require a valid JWT bearer token.

### 2. Authentication Filter (`JwtAuthenticationFilter.java`)
Every non-permitted request passes through the `OncePerRequestFilter`:
1. **Header Parsing**: Extracts the `Authorization` header and checks for the `Bearer ` prefix.
2. **Token Extraction**: Decodes and parses the JWT token.
3. **User Extraction**: Calls `jwtHelper.extractUserId` to extract the `userId` (subject) of the token.
4. **Account State Validation**: 
   - Queries the database to fetch user details.
   - If the account has been deactivated (`isEnabled() == false`), the filter intercepts the request, blocks further execution, and returns a JSON payload: `{"error": "AccountDisabled", "message": "Account is disabled"}` with a `401 Unauthorized` status.
5. **Context Binding**: Registers the user principal into Spring's `SecurityContextHolder`.

### 3. JWT Error Handlers
If the JWT validation fails, the filter directly writes the following JSON responses (with `401 Unauthorized` status):
- **Expired Token**: `{"error": "TokenExpired", "message": "Token đã hết hạn"}`
- **Invalid Token**: `{"error": "InvalidToken", "message": "Token không hợp lệ"}`

---

## JWT Signature Verification

- Signatures are verified using a HMAC-SHA key.
- To maintain standard-compliant key lengths, the backend expects a **Base64-encoded secret** from the environment properties (`app.jwt.secret`).
- The signature key is generated programmatically:
  ```java
  private SecretKey getSigningKey() {
      return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
  }
  ```

---

## Refresh Token Rotation (RTR) & Storage Security

To protect against token hijacking and session theft, the backend implements a strict **Refresh Token Rotation** flow:

```
[Client submits Raw Refresh Token]
              │
              ▼
[Hash Raw Token using SHA-256 & Base64 Encode]
              │
              ▼
[Verify TokenHash in DB (Not expired & Not revoked)]
              │
              ▼
[Revoke Old Token: set revokedAt = now]
              │
              ▼
[Generate New Access Token & New Raw Refresh Token]
              │
              ▼
[Hash & Save New Refresh Token to DB]
              │
              ▼
[Return New Pair to Client]
```

### Key Security Implementations:
1. **Token Hashing in DB**: Refresh tokens are never stored in plain text. When a refresh token is generated (`UUID.randomUUID().toString()`), it is hashed using **SHA-256** and Base64-encoded before being saved to the `refresh_tokens.token_hash` column. Even in the event of a database compromise, attackers cannot use the hashes to authenticate.
2. **One-Time Use Tokens (Rotation)**: When a client calls `/auth/refresh` with a valid raw refresh token:
   - The token is verified.
   - It is immediately revoked by setting `revokedAt = OffsetDateTime.now()`.
   - A brand new access token and refresh token pair are generated and returned.
   - This prevents replay attacks where an intercepted refresh token is used multiple times.
3. **Logout Invalidation**: When a user logs out (`DELETE /auth/logout`), the raw refresh token is hashed and its status in the DB is set to revoked.

---

## Controller Argument Binding: `@CurrentUser`

To simplify developer experience and prevent manual parsing of user credentials in services, the system features a custom annotation:
- `@CurrentUser Long userId` can be declared directly in controller method arguments.
- It is resolved by `CurrentUserArgumentResolver.java` which automatically extracts the principal from Spring SecurityContext and binds the user's ID as a `Long`.
