# User Profile API

Handles user profile retrieval, setting updates, and profile avatar uploads.

All endpoints in this section are prefixed with `/users` and require a valid JWT Bearer Token.

---

## 1. Retrieve Current Profile

Fetches the authenticated user's private profile details.

- **URL**: `/me`
- **Method**: `GET`

### Response Payload (`200 OK`)
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "Request processed successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "Jane Doe",
    "avatarUrl": "https://cloudinary.com/avatar.png",
    "timezone": "Asia/Ho_Chi_Minh",
    "streakThresholdMinutes": 25,
    "isVerified": false,
    "isActive": true
  }
}
```

---

## 2. Update Profile Settings

Modifies profile display name, timezone, or daily focus threshold.

- **URL**: `/me`
- **Method**: `PUT`

### Request Payload (`UpdateProfileRequest`)
```json
{
  "name": "Jane Smith",
  "timezone": "America/New_York",
  "streakThresholdMinutes": 45
}
```
*Constraints*:
- `name`: Must be non-blank.
- `timezone`: Optional, must be a valid Timezone ID (e.g., `Europe/London`).
- `streakThresholdMinutes`: Optional, positive integer.

### Response Payload (`200 OK`)
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "Request processed successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "Jane Smith",
    "avatarUrl": "https://cloudinary.com/avatar.png",
    "timezone": "America/New_York",
    "streakThresholdMinutes": 45,
    "isVerified": false,
    "isActive": true
  }
}
```

---

## 3. Upload or Change Avatar

Uploads a new avatar file to Cloudinary. Automatically parses old avatar URLs, extracts public IDs, and deletes the old file from Cloudinary storage to conserve space.

- **URL**: `/me/avatar`
- **Method**: `PUT`
- **Content-Type**: `multipart/form-data`

### Multipart Request Form
- Field Key: `file`
- Value: *Binary Image File* (JPEG, PNG, WEBP)

### Response Payload (`200 OK`)
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "Request processed successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "Jane Smith",
    "avatarUrl": "https://res.cloudinary.com/ddeucv71n/image/upload/v12345/minute-mind/avatars/new_image.jpg",
    "timezone": "America/New_York",
    "streakThresholdMinutes": 45,
    "isVerified": false,
    "isActive": true
  }
}
```

---

## 4. Remove Avatar

Deletes the user's avatar from Cloudinary storage and sets `avatarUrl = null` in the database.

- **URL**: `/me/avatar`
- **Method**: `DELETE`

### Response Payload (`200 OK`)
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "Request processed successfully",
  "data": null
}
```
