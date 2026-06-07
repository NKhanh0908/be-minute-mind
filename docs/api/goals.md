# Goals API

Handles the creation, updates, reordering, and soft-deletion of user objectives.

All endpoints in this section are prefixed with `/goals` and require a valid JWT Bearer Token.

---

## 1. Retrieve User Goals

Fetches all active (non-deleted) goals of the authenticated user, ordered by user's sort preference.

- **URL**: `/`
- **Method**: `GET`

### Response Payload (`200 OK`)
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "Request processed successfully",
  "data": [
    {
      "id": 1,
      "title": "Build Portfolio App",
      "description": "Develop and deploy portfolio website using React",
      "color": "#4A90E2",
      "targetTotalMinutes": 600,
      "deadline": "2026-12-31",
      "status": "ACTIVE",
      "sortOrder": 0,
      "isShared": false,
      "totalLoggedMinutes": 45,
      "createdAt": "2026-06-07T05:00:00Z",
      "updatedAt": "2026-06-07T05:45:00Z"
    }
  ]
}
```
*Note*: `totalLoggedMinutes` is dynamically computed by summing focus sessions spent on all subtasks of this goal.

---

## 2. Create Goal

Instantiates a new objective. The new goal is automatically appended to the end of the user's goal list (`sortOrder` set to current size).

- **URL**: `/`
- **Method**: `POST`

### Request Payload (`GoalRequest`)
```json
{
  "title": "Build Portfolio App",
  "description": "Develop and deploy portfolio website using React",
  "color": "#4A90E2",
  "targetTotalMinutes": 600,
  "deadline": "2026-12-31"
}
```
*Constraints*:
- `title`: Must be non-blank.

### Response Payload (`201 Created`)
```json
{
  "status": "SUCCESS",
  "code": 201,
  "message": "Resource created successfully",
  "data": {
    "id": 1,
    "title": "Build Portfolio App",
    "description": "Develop and deploy portfolio website using React",
    "color": "#4A90E2",
    "targetTotalMinutes": 600,
    "deadline": "2026-12-31",
    "status": "ACTIVE",
    "sortOrder": 0,
    "isShared": false,
    "totalLoggedMinutes": 0,
    "createdAt": "2026-06-07T12:00:00Z",
    "updatedAt": "2026-06-07T12:00:00Z"
  }
}
```

---

## 3. Update Goal

Modifies an existing goal's attributes.

- **URL**: `/{id}`
- **Method**: `PUT`

### Request Payload (`GoalRequest`)
```json
{
  "title": "Build Portfolio App (V2)",
  "description": "Complete rewrite of portfolio website in Next.js",
  "color": "#5C6BC0",
  "targetTotalMinutes": 1000,
  "deadline": "2026-09-30"
}
```

### Response Payload (`200 OK`)
```json
{
  "status": "SUCCESS",
  "code": 200,
  "message": "Request processed successfully",
  "data": {
    "id": 1,
    "title": "Build Portfolio App (V2)",
    "description": "Complete rewrite of portfolio website in Next.js",
    "color": "#5C6BC0",
    "targetTotalMinutes": 1000,
    "deadline": "2026-09-30",
    "status": "ACTIVE",
    "sortOrder": 0,
    "isShared": false,
    "totalLoggedMinutes": 45,
    "createdAt": "2026-06-07T12:00:00Z",
    "updatedAt": "2026-06-07T12:19:00Z"
  }
}
```

---

## 4. Delete Goal (Soft Delete)

Soft deletes a goal by marking its `deletedAt` field. Associated tasks are also excluded from active queries, but focus history is preserved.

- **URL**: `/{id}`
- **Method**: `DELETE`

### Response (`204 No Content`)
*(No body)*

---

## 5. Update Sort Order

Reorders the user's active goals sequentially based on a list of IDs.

- **URL**: `/sort`
- **Method**: `PATCH`

### Request Payload (`SortOrderRequest`)
```json
{
  "ids": [3, 1, 2]
}
```

### Response (`204 No Content`)
*(No body)*
