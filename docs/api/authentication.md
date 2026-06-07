# Authentication API

Handles user onboarding, authentication, and secure token lifecycle.

All endpoints in this section are prefixed with `/auth`.

---

## 1. Register User

Creates a new user profile.

- **URL**: `/register`
- **Method**: `POST`
- **Authentication**: None
- **Rate Limit**: Max 10 requests per 60 seconds (by IP)

### Request Payload (`RegisterRequest`)
```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "name": "Jane Doe"
}
```
*Constraints*:
- `email`: Valid format, must not already exist in the database.
- `password`: Must be non-blank.
- `name`: Must be non-blank.

### Response Payload (`AuthResponse` with `201 Created` Status)
```json
{
  "status": "SUCCESS",
  "code": 201,
  "message": "Resource created successfully",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "48b1112d-947b-402a-9cb8-b27b8753a73c",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "Jane Doe",
      "avatarUrl": null,
      "timezone": "Asia/Ho_Chi_Minh",
      "streakThresholdMinutes": 25,
      "isVerified": false,
      "isActive": true
    }
  }
}
```

---

## 2. Login User

Authenticates a user and returns a token pair.

- **URL**: `/login`
- **Method**: `POST`
- **Authentication**: None
- **Rate Limit**: Max 10 requests per 60 seconds (by IP)

### Request Payload (`LoginRequest`)
```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

### Response Payload (`AuthResponse` with `200 OK` Status)
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "Request processed successfully",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "48b1112d-947b-402a-9cb8-b27b8753a73c",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "Jane Doe",
      "avatarUrl": null,
      "timezone": "Asia/Ho_Chi_Minh",
      "streakThresholdMinutes": 25,
      "isVerified": false,
      "isActive": true
    }
  }
}
```
*Business Rules*:
- Throws `401 Unauthorized` if email or password does not match.
- Throws `400 Bad Request` if the user account is deactivated (`isActive = false`).

---

## 3. Refresh Token (Token Rotation)

Exchanges a valid refresh token for a brand new token pair.

- **URL**: `/refresh`
- **Method**: `POST`
- **Authentication**: None
- **Rate Limit**: Max 10 requests per 60 seconds (by IP)

### Request Payload (`RefreshTokenRequest`)
```json
{
  "refreshToken": "48b1112d-947b-402a-9cb8-b27b8753a73c"
}
```

### Response Payload (`AuthResponse` with `200 OK` Status)
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "Request processed successfully",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "a794b12d-947b-402a-9cb8-b27b8753a99e",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "Jane Doe",
      "avatarUrl": null,
      "timezone": "Asia/Ho_Chi_Minh",
      "streakThresholdMinutes": 25,
      "isVerified": false,
      "isActive": true
    }
  }
}
```
*Business Rules*:
- The old refresh token is immediately revoked upon validation.
- If the token is expired, revoked, or invalid, a `401 Unauthorized` status is returned.

---

## 4. Logout User

Logs out a user and invalidates the specified refresh token.

- **URL**: `/logout`
- **Method**: `DELETE`
- **Authentication**: Required (JWT Bearer Token in `Authorization` header)

### Request Payload (`RefreshTokenRequest`)
```json
{
  "refreshToken": "a794b12d-947b-402a-9cb8-b27b8753a99e"
}
```

### Response (with `204 No Content` Status)
*(No body)*
