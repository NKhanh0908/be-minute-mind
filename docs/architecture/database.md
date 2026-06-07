# Database Design Specification

## Entity Relationship Diagram (ERD)

This diagram displays the database schema and entity relationships for the **MinuteMind** backend, mapped from the active JPA entities.

```mermaid
erDiagram
    users ||--o| streaks : "has one"
    users ||--o{ goals : "owns"
    users ||--o{ goal_members : "participates in"
    users ||--o{ goal_invitations : "invites / receives"
    users ||--o{ user_badges : "earns"
    users ||--o{ work_sessions : "performs"
    users ||--o{ refresh_tokens : "has"
    users ||--o{ oauth_connections : "links"
    users ||--o{ notifications : "receives"
    users ||--o{ streak_activities : "logs"
    users ||--o{ goal_progress : "tracks"
    users ||--o{ user_relationships : "follows / followed by"

    goals ||--o{ tasks : "contains"
    goals ||--o{ goal_members : "has members"
    goals ||--o{ goal_invitations : "undergoes"
    goals ||--o{ goal_progress : "accumulates"

    tasks ||--o{ work_sessions : "associated with"

    achievement_badges ||--o{ user_badges : "defines"

    users {
        Long id PK
        String email UK
        String passwordHash
        String name
        String avatarUrl
        String timezone
        Integer streakThresholdMinutes
        Boolean isVerified
        Boolean isActive
        OffsetDateTime createdAt
        OffsetDateTime updatedAt
        OffsetDateTime deletedAt
    }

    streaks {
        Long id PK
        Long userId FK "UK"
        Integer currentStreak
        Integer longestStreak
        LocalDate lastActiveDate
        Integer totalActiveDays
        OffsetDateTime createdAt
        OffsetDateTime updatedAt
    }

    goals {
        Long id PK
        Long userId FK
        String title
        String description
        String color
        Integer targetTotalMinutes
        LocalDate deadline
        GoalStatus status
        Integer sortOrder
        Boolean isShared
        OffsetDateTime createdAt
        OffsetDateTime updatedAt
        OffsetDateTime deletedAt
    }

    tasks {
        Long id PK
        Long goalId FK
        Long userId FK
        String title
        String description
        Integer estimatedMinutes
        Integer totalLoggedMinutes
        TaskStatus status
        Integer sortOrder
        OffsetDateTime createdAt
        OffsetDateTime updatedAt
        OffsetDateTime deletedAt
    }

    work_sessions {
        Long id PK
        Long userId FK
        Long taskId FK
        SessionType sessionType
        Integer plannedMinutes
        Integer actualMinutes
        OffsetDateTime startedAt
        OffsetDateTime endedAt
        OffsetDateTime lastHeartbeatAt
        Boolean completed
        String notes
        OffsetDateTime createdAt
        OffsetDateTime updatedAt
    }

    goal_members {
        Long id PK
        Long goalId FK "UK"
        Long userId FK "UK"
        GoalMemberRole role
        OffsetDateTime joinedAt
    }

    goal_invitations {
        Long id PK
        Long goalId FK "UK"
        Long inviterId FK
        Long inviteeId FK "UK"
        InvitationStatus status
        OffsetDateTime createdAt
        OffsetDateTime respondedAt
    }

    goal_progress {
        Long id PK
        Long goalId FK "UK"
        Long userId FK
        LocalDate trackedDate "UK"
        Integer actualMinutes
        OffsetDateTime createdAt
        OffsetDateTime updatedAt
    }

    achievement_badges {
        Long id PK
        String code UK
        String name
        String description
        String icon
        BadgeRarity rarity
        BadgeConditionType conditionType
        Integer conditionValue
        OffsetDateTime createdAt
    }

    user_badges {
        Long id PK
        Long userId FK "UK"
        Long badgeId FK "UK"
        OffsetDateTime earnedAt
        Boolean seen
    }

    user_relationships {
        Long id PK
        Long followerId FK "UK"
        Long followingId FK "UK"
        OffsetDateTime createdAt
    }

    refresh_tokens {
        Long id PK
        Long userId FK
        String tokenHash UK
        String deviceInfo
        OffsetDateTime expiresAt
        OffsetDateTime revokedAt
        OffsetDateTime createdAt
    }

    oauth_connections {
        Long id PK
        Long userId FK
        String provider "UK"
        String providerUserId "UK"
        String accessToken
        String refreshToken
        OffsetDateTime expiresAt
        OffsetDateTime createdAt
        OffsetDateTime updatedAt
    }

    notifications {
        Long id PK
        Long userId FK
        NotificationType type
        String title
        String body
        String payload
        Boolean isRead
        OffsetDateTime createdAt
    }

    streak_activities {
        Long id PK
        Long userId FK "UK"
        LocalDate activityDate "UK"
        Integer totalMinutes
        Boolean isValid
        OffsetDateTime createdAt
        OffsetDateTime updatedAt
    }
```

---

## Database Tables & Core Columns

### 1. `users`
Stores user profile credentials, settings, and account status.
- `id` (BIGINT, Primary Key, Auto-Increment)
- `email` (VARCHAR, Unique, Non-nullable)
- `password_hash` (VARCHAR)
- `name` (VARCHAR, Non-nullable)
- `avatar_url` (VARCHAR)
- `timezone` (VARCHAR, Default: 'Asia/Ho_Chi_Minh')
- `streak_threshold_minutes` (INTEGER, Default: 25)
- `is_verified` (BOOLEAN, Default: false)
- `is_active` (BOOLEAN, Default: true)
- `deleted_at` (TIMESTAMP WITH TIME ZONE, Nullable for soft deletes)

### 2. `goals`
Containers for user objectives.
- `id` (BIGINT, Primary Key)
- `user_id` (BIGINT, Foreign Key referencing `users.id`)
- `title` (VARCHAR, Non-nullable)
- `description` (TEXT)
- `color` (VARCHAR, hexadecimal or color code)
- `target_total_minutes` (INTEGER, Nullable target)
- `deadline` (DATE, Nullable)
- `status` (VARCHAR, mapped from `GoalStatus` enum: `ACTIVE`, `PAUSED`, `COMPLETED`, `ARCHIVED`)
- `sort_order` (INTEGER, Default: 0)
- `is_shared` (BOOLEAN, Default: false)
- `deleted_at` (TIMESTAMP WITH TIME ZONE, Nullable)

### 3. `tasks`
Individual tasks that belong to goals.
- `id` (BIGINT, Primary Key)
- `goal_id` (BIGINT, Foreign Key referencing `goals.id`)
- `user_id` (BIGINT, Foreign Key referencing `users.id`)
- `title` (VARCHAR, Non-nullable)
- `description` (TEXT)
- `estimated_minutes` (INTEGER)
- `total_logged_minutes` (INTEGER, Default: 0)
- `status` (VARCHAR, mapped from `TaskStatus` enum: `TODO`, `IN_PROGRESS`, `DONE`)
- `sort_order` (INTEGER, Default: 0)
- `deleted_at` (TIMESTAMP WITH TIME ZONE, Nullable)

### 4. `work_sessions`
Focus and break session records associated with tasks.
- `id` (BIGINT, Primary Key)
- `user_id` (BIGINT, Foreign Key)
- `task_id` (BIGINT, Foreign Key)
- `session_type` (VARCHAR, `WORK` or `BREAK`)
- `planned_minutes` (INTEGER, Non-nullable)
- `actual_minutes` (INTEGER, Default: 0)
- `started_at` (TIMESTAMP WITH TIME ZONE, Non-nullable)
- `ended_at` (TIMESTAMP WITH TIME ZONE, Nullable)
- `last_heartbeat_at` (TIMESTAMP WITH TIME ZONE, Non-nullable)
- `completed` (BOOLEAN, Default: false)
- `notes` (TEXT)

---

## Constraints & Business Safeguards

To maintain data integrity at the database layer, the following **Unique Constraints** are defined:
- **`goal_members`**: Unique composite constraint on `(goal_id, user_id)` ensures a user cannot join the same goal multiple times.
- **`goal_invitations`**: Unique composite constraint on `(goal_id, invitee_id)` restricts invitations to one active/pending invite per user per goal.
- **`goal_progress`**: Unique composite constraint on `(goal_id, tracked_date)` prevents duplicate progress logs for the same goal on the same day.
- **`streak_activities`**: Unique composite constraint on `(user_id, activity_date)` ensures a user has exactly one focus minutes total record per calendar day.
- **`user_badges`**: Unique composite constraint on `(user_id, badge_id)` prevents the duplicate awarding of the same badge.
- **`user_relationships`**: Unique composite constraint on `(follower_id, following_id)` restricts followers to a single association.
- **`oauth_connections`**: Unique composite constraint on `(provider, provider_user_id)` prevents linking a single third-party provider account to multiple local accounts.

---

## Database Performance Indexes

To handle high-frequency reads and writes (such as heartbeats, dashboard loadings, and feeds), the system utilizes the following indexes:

### 1. Work Session Queries
`idx_sessions_user_date` on `(userId, startedAt DESC)`
- **Why**: Speeds up statistics generation, dashboard history queries, and heatmap rendering, which frequently request the user's latest sessions.

`idx_sessions_task` on `(taskId)`
- **Why**: Speeds up aggregation calculations when summing the total logged minutes for a specific task.

### 2. Live Notifications
`idx_notifications_user` on `(userId, isRead, createdAt DESC)`
- **Why**: Speeds up notification inbox rendering, letting the system fetch unread alerts instantly, sorted by the newest first.

### 3. Social Follow Graph
`idx_following` on `(followingId)`
- **Why**: Speeds up lookups on who is following a user, which is critical for compiling daily focus leaderboards and activity feeds.
