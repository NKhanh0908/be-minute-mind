# Entity Relationship Diagram (ERD)

This document represents the database schema and entity relationships for the **MinuteMind** backend. It is derived directly from the JPA entities in the codebase.

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
