# Focus Session Workflow

This diagram outlines the lifecycle of a focus session (`WorkSession`) from start to completion or discard.

```mermaid
flowchart TD
    Start([User starts Session]) --> ReqStart[POST /sessions/start]
    ReqStart --> DBCheck{Active Session in DB?}
    
    DBCheck -- Yes --> Error[Throw ValidationException: 'Session already active']
    DBCheck -- No --> TaskCheck{Task exists & belongs to User?}
    
    TaskCheck -- No --> Error404[Throw ResourceNotFoundException: 'Task not found']
    TaskCheck -- Yes --> CreateSession[Create WorkSession in DB]
    
    CreateSession --> UpdateTask[Set Task Status to 'IN_PROGRESS' if 'TODO']
    UpdateTask --> CacheRedis[Store Session ID in Redis: vilo:active_session:userId for 4 Hours]
    CacheRedis --> ActiveSession[Session is Active]
    
    ActiveSession --> Loop[User focusing...]
    Loop --> Heartbeat[POST /sessions/{id}/heartbeat]
    Heartbeat --> UpdateHeartbeat[Update actualMinutes & lastHeartbeatAt in DB]
    UpdateHeartbeat --> ExtendRedis[Reset Redis TTL to 4 Hours]
    ExtendRedis --> ActiveSession
    
    ActiveSession -- User finishes/stops --> Complete[POST /sessions/{id}/complete]
    ActiveSession -- User cancels --> Discard[POST /sessions/{id}/discard]
    
    Discard --> SetEnded[Set endedAt = now, completed = false in DB]
    SetEnded --> DelRedis[Delete Redis Key]
    DelRedis --> DoneDiscard([Session Discarded])
    
    Complete --> SaveComp[Save actualMinutes, endedAt, notes in DB]
    SaveComp --> CheckTaskComp{User marked Task completed?}
    
    CheckTaskComp -- Yes --> SetTaskDone[Set Task Status to 'DONE', session.completed = true]
    CheckTaskComp -- No --> ReachedTimer{actualMinutes >= plannedMinutes - 1?}
    ReachedTimer -- Yes --> SetSessionComp[Set session.completed = true]
    ReachedTimer -- No --> SetSessionFail[Set session.completed = false]
    
    SetTaskDone --> SaveSession[Save WorkSession & Add totalLoggedMinutes to Task]
    SetSessionComp --> SaveSession
    SetSessionFail --> SaveSession
    
    SaveSession --> DelRedis2[Delete Redis Key]
    DelRedis2 --> CheckGamify{Is WORK session & actualMinutes > 0?}
    
    CheckGamify -- Yes --> StreakLogic[Update Streak]
    StreakLogic --> BadgeLogic[Check & Award Badges]
    BadgeLogic --> Finished([Session Completed])
    
    CheckGamify -- No --> Finished
```
