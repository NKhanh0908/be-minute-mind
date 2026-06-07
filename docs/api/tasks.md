# Tasks API

Handles the lifecycles, progress status updates, and custom reordering of tasks within goals.

All endpoints in this section are prefixed with `/tasks` and require a valid JWT Bearer Token.

---

## 1. Retrieve Tasks by Goal

Fetches all active (non-deleted) tasks for a specific goal owned by the user, ordered by `sortOrder` ascending.

- **URL**: `/goal/{goalId}`
- **Method**: `GET`

### Response Payload (`200 OK`)
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "Request processed successfully",
  "data": [
    {
      "id": 10,
      "goalId": 1,
      "title": "Design Database Schema",
      "description": "Establish tables and relationship keys",
      "estimatedMinutes": 60,
      "totalLoggedMinutes": 0,
      "status": "TODO",
      "sortOrder": 0,
      "createdAt": "2026-06-07T12:00:00Z",
      "updatedAt": "2026-06-07T12:00:00Z"
    }
  ]
}
```

---

## 2. Create Task

Creates a new task. The new task is automatically appended to the end of the goal's task list (`sortOrder` set to current size) and starts with the status `TODO`.

- **URL**: `/`
- **Method**: `POST`

### Request Payload (`TaskRequest`)
```json
{
  "goalId": 1,
  "title": "Design Database Schema",
  "description": "Establish tables and relationship keys",
  "estimatedMinutes": 60
}
```
*Constraints*:
- `goalId`: Must be non-null. Must belong to the authenticated user.
- `title`: Must be non-blank.

### Response Payload (`201 Created`)
```json
{
  "status": "SUCCESS",
  "code": 201,
  "message": "Resource created successfully",
  "data": {
    "id": 10,
    "goalId": 1,
    "title": "Design Database Schema",
    "description": "Establish tables and relationship keys",
    "estimatedMinutes": 60,
    "totalLoggedMinutes": 0,
    "status": "TODO",
    "sortOrder": 0,
    "createdAt": "2026-06-07T12:10:00Z",
    "updatedAt": "2026-06-07T12:10:00Z"
  }
}
```

---

## 3. Update Task Details

Updates title, description, estimates, or moves the task to a different goal by updating the `goalId`.

- **URL**: `/{id}`
- **Method**: `PUT`

### Request Payload (`TaskRequest`)
```json
{
  "goalId": 1,
  "title": "Design Database Schema (Revised)",
  "description": "Establish tables, relationship keys, and partition constraints",
  "estimatedMinutes": 90
}
```
*Constraints*:
- If moving to a new `goalId`, the target goal must exist, belong to the authenticated user, and not be deleted.

### Response Payload (`200 OK`)
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "Request processed successfully",
  "data": {
    "id": 10,
    "goalId": 1,
    "title": "Design Database Schema (Revised)",
    "description": "Establish tables, relationship keys, and partition constraints",
    "estimatedMinutes": 90,
    "totalLoggedMinutes": 0,
    "status": "TODO",
    "sortOrder": 0,
    "createdAt": "2026-06-07T12:10:00Z",
    "updatedAt": "2026-06-07T12:20:00Z"
  }
}
```

---

## 4. Update Task Status

Manually overrides a task's status state.

- **URL**: `/{id}/status`
- **Method**: `PATCH`
- **Query Parameter**: `status` (values: `TODO`, `IN_PROGRESS`, `DONE`)

### Response Payload (`200 OK`)
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "Request processed successfully",
  "data": {
    "id": 10,
    "goalId": 1,
    "title": "Design Database Schema (Revised)",
    "description": "Establish tables, relationship keys, and partition constraints",
    "estimatedMinutes": 90,
    "totalLoggedMinutes": 0,
    "status": "DONE",
    "sortOrder": 0,
    "createdAt": "2026-06-07T12:10:00Z",
    "updatedAt": "2026-06-07T12:21:00Z"
  }
}
```

---

## 5. Delete Task (Soft Delete)

Soft deletes a task by marking its `deletedAt` field.

- **URL**: `/{id}`
- **Method**: `DELETE`

### Response (`204 No Content`)
*(No body)*

---

## 6. Update Sort Order

Reorders active tasks within a specific goal sequentially based on a list of IDs.

- **URL**: `/goal/{goalId}/sort`
- **Method**: `PATCH`

### Request Payload (`SortOrderRequest`)
```json
{
  "ids": [12, 10, 11]
}
```

### Response (`204 No Content`)
*(No body)*
