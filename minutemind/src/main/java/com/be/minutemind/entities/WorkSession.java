package com.be.minutemind.entities;

import com.be.minutemind.enums.SessionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "work_sessions", indexes = {
        @Index(name = "idx_sessions_user_date", columnList = "userId, startedAt DESC"),
        @Index(name = "idx_sessions_task", columnList = "taskId")
})
public class WorkSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long taskId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionType sessionType;

    @Column(nullable = false)
    private Integer plannedMinutes;

    @Column(nullable = false)
    @Builder.Default
    private Integer actualMinutes = 0;

    @Column(nullable = false)
    private OffsetDateTime startedAt;

    private OffsetDateTime endedAt;

    @Column(nullable = false)
    private OffsetDateTime lastHeartbeatAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;

    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    public boolean isActive() {
        return endedAt == null;
    }

    public boolean isWorkSession() {
        return SessionType.WORK.equals(sessionType);
    }

    public boolean isOrphaned() {
        return isActive() && lastHeartbeatAt.isBefore(OffsetDateTime.now().minusMinutes(5));
    }

    public void completeSession(int actualMinutes, boolean completed) {
        this.actualMinutes = actualMinutes;
        this.completed = completed;
        this.endedAt = OffsetDateTime.now();
    }
}
