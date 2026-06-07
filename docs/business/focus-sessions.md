# Focus Sessions & Real-Time Tracking

The **Focus Session** (`WorkSession`) is the core engine of the MinuteMind system, turning abstract plans into tracked effort.

---

## Session Lifecycle States

An active focus session has a strict state machine.

```
[Start Session] ➔ [Active Session] ➔ [Complete Session] OR [Discard Session]
```

### 1. Starting a Session (`POST /sessions/start`)
- **Single-Active-Session Enforcement**: A user is restricted to a single active session at any given time.
  - The backend queries the database for any session with `endedAt = null` for the user. If found, a `400 Bad Request` (`ValidationException`) is thrown.
  - This prevents users from duplicating time logs by running multiple timers in parallel.
- **Vite/React Client Sync**:
  - The backend saves the new session in the database and caches the `sessionId` in Redis under the key `vilo:active_session:<userId>` with a Time-To-Live (TTL) of **4 hours**.
  - This Redis key acts as a distributed lock and allows the client to quickly query the current session on startup or page refresh.
- **Task Association**: The associated task's status is automatically promoted from `TODO` to `IN_PROGRESS`.

### 2. Heartbeat Updates (`POST /sessions/{id}/heartbeat`)
Because clients run in web browsers which can freeze, crash, or lose network connectivity, the system uses a **Heartbeat** mechanism to validate time.
- The client sends a heartbeat request containing the current `actualMinutes` at regular intervals.
- The backend updates the session's `actualMinutes` and resets `lastHeartbeatAt` to the current timestamp.
- The Redis active session cache key's expiration is reset back to **4 hours**.

### 3. Completing a Session (`POST /sessions/{id}/complete`)
When the timer completes, the user submits a completion request:
- **Task Auto-Completion**: If the user checks the "Task Completed" box, the task status is immediately set to `DONE`, and the session is marked as `completed = true`.
- **Grace Period (Grace Margin)**: If the task is not completed, the session is marked as completed only if the user stayed focused for the planned duration. The system allows a **1-minute grace margin** (`actualMinutes >= plannedMinutes - 1`) to account for small latency delays in network transit.
- **Time Logging**: The session's `actualMinutes` are added directly to the task's `totalLoggedMinutes` using a native thread-safe increment query (`addLoggedMinutes`).
- **Cache Clean Up**: The Redis active session key is deleted.
- **Gamification Trigger**: If the session type is `WORK` and `actualMinutes > 0`, the backend triggers the **Streak** and **Badge** evaluation services.

### 4. Discarding a Session (`POST /sessions/{id}/discard`)
If a user cancels a session:
- The session is terminated with `endedAt = now` and `completed = false`.
- The Redis cache key is deleted.
- No time is added to the task, and no gamification checks are run.

---

## Handling Orphaned Sessions

An active session is defined as **Orphaned** if its `lastHeartbeatAt` is older than **5 minutes** from the current time (`isOrphaned()`).
- This indicates that the client closed the application, disconnected, or crashed without formally completing or discarding the session.
- Orphaned sessions are detected on client requests (e.g., when the user attempts to view their current session or start a new one). The application handles these by terminating or cleaning them up to allow the user to start a fresh session.
