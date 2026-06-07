# Focus Sessions API

Handles real-time session tracking, heartbeat check-ins, completion, and discard operations.

All endpoints in this section are prefixed with `/sessions` and require a valid JWT Bearer Token.

---

## 1. Get Current Session

Fetches the currently active focus session for the user, if one exists in the database.

- **URL**: `/current`
- **Method**: `GET`

### Response Payload (`200 OK`)
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "Request processed successfully",
  "data": {
    "id": 100,
    "taskId": 10,
    "sessionType": "WORK",
    "plannedMinutes": 25,
    "startedAt": "2026-06-07T12:00:00Z",
    "lastHeartbeatAt": "2026-06-07T12:15:00Z"
  }
}
```
*Note*: Returns `data: null` if there is no active session.

---

## 2. Start Focus Session

Starts a new focus session. Automatically caches the session ID in Redis for Vite clients and marks the task as `IN_PROGRESS` if it was previously in `TODO`.

- **URL**: `/start`
- **Method**: `POST`

### Request Payload (`SessionStartRequest`)
```json
{
  "taskId": 10,
  "sessionType": "WORK",
  "plannedMinutes": 25
}
```
*Constraints*:
- `taskId`: Must be non-null. Must belong to the user.
- `sessionType`: Must be either `WORK` or `BREAK`.
- `plannedMinutes`: Must be non-null and greater than 0.

### Response Payload (`201 Created`)
```json
{
  "status": "SUCCESS",
  "code": 201,
  "message": "Resource created successfully",
  "data": {
    "id": 100,
    "taskId": 10,
    "sessionType": "WORK",
    "plannedMinutes": 25,
    "startedAt": "2026-06-07T12:00:00Z",
    "lastHeartbeatAt": "2026-06-07T12:00:00Z"
  }
}
```
*Business Rules*:
- Throws `400 Bad Request` if the user already has an active session in the database.

---

## 3. Session Heartbeat

Sends a periodic update to keep the active session alive and track elapsed time.

- **URL**: `/{id}/heartbeat`
- **Method**: `POST`
- **Query Parameter**: `currentActualMinutes` (int, current count of focused minutes elapsed)

### Response (`204 No Content`)
*(No body)*

---

## 4. Complete Focus Session

Stops and saves progress for the active focus session. Evaluates task completion, adds focused time to the task total, removes the Redis lock, and triggers gamification routines.

- **URL**: `/{id}/complete`
- **Method**: `POST`

### Request Payload (`SessionCompleteRequest`)
```json
{
  "actualMinutes": 25,
  "notes": "Finished database schema design",
  "completedTask": true
}
```
*Fields*:
- `actualMinutes`: Actual focused minutes spent.
- `notes`: Text notes about what was accomplished.
- `completedTask`: Boolean. If true, the associated task status is set to `DONE`.

### Response (`204 No Content`)
*(No body)*

---

## 5. Discard Focus Session

Cancels the active focus session. Progress is discarded, and no focus minutes are added to the task.

- **URL**: `/{id}/discard`
- **Method**: `POST`

### Response (`204 No Content`)
*(No body)*
