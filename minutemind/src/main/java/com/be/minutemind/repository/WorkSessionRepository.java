package com.be.minutemind.repository;

import com.be.minutemind.entities.WorkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkSessionRepository extends JpaRepository<WorkSession, Long> {

    Optional<WorkSession> findByUserIdAndEndedAtIsNull(Long userId);

    @Modifying
    @Query("UPDATE WorkSession ws SET ws.actualMinutes = :minutes, ws.lastHeartbeatAt = :now WHERE ws.id = :id")
    void updateHeartbeat(@Param("id") Long id, @Param("minutes") int minutes, @Param("now") OffsetDateTime now);

    @Query("""
        SELECT COALESCE(SUM(ws.actualMinutes), 0)
        FROM WorkSession ws
        WHERE ws.userId = :userId
          AND ws.sessionType = com.be.minutemind.enums.SessionType.WORK
          AND ws.startedAt >= :startOfDay AND ws.startedAt < :endOfDay
    """)
    Integer sumWorkMinutesForDate(@Param("userId") Long userId, @Param("startOfDay") OffsetDateTime startOfDay, @Param("endOfDay") OffsetDateTime endOfDay);

    @Query("""
        SELECT ws FROM WorkSession ws
        WHERE ws.userId = :userId
          AND ws.startedAt >= :startOfDay AND ws.startedAt < :endOfDay
        ORDER BY ws.startedAt DESC
    """)
    List<WorkSession> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startOfDay") OffsetDateTime startOfDay, @Param("endOfDay") OffsetDateTime endOfDay);

    @Query("SELECT COALESCE(SUM(ws.actualMinutes), 0) FROM WorkSession ws WHERE ws.userId = :userId AND ws.sessionType = com.be.minutemind.enums.SessionType.WORK")
    Integer sumTotalWorkMinutes(@Param("userId") Long userId);

    @Query("""
        SELECT COALESCE(SUM(ws.actualMinutes), 0)
        FROM WorkSession ws
        WHERE ws.userId = :userId
          AND ws.sessionType = com.be.minutemind.enums.SessionType.WORK
          AND ws.startedAt >= :from
    """)
    Integer sumWorkMinutesFrom(@Param("userId") Long userId, @Param("from") OffsetDateTime from);

    @Query("""
        SELECT ws.userId, COALESCE(SUM(ws.actualMinutes), 0)
        FROM WorkSession ws
        WHERE ws.userId IN :userIds
          AND ws.sessionType = com.be.minutemind.enums.SessionType.WORK
          AND ws.startedAt >= :startOfDay AND ws.startedAt < :endOfDay
        GROUP BY ws.userId
    """)
    List<Object[]> sumWorkMinutesForDateByUsers(@Param("userIds") List<Long> userIds, @Param("startOfDay") OffsetDateTime startOfDay, @Param("endOfDay") OffsetDateTime endOfDay);

    /**
     * Tổng phút WORK của mỗi user trong 1 goal cụ thể (qua task).
     * Dùng cho bảng tiến độ thành viên trong Shared Goal.
     */
    @Query("""
        SELECT ws.userId, COALESCE(SUM(ws.actualMinutes), 0)
        FROM WorkSession ws
        JOIN Task t ON t.id = ws.taskId
        WHERE ws.userId IN :userIds
          AND t.goalId = :goalId
          AND ws.sessionType = com.be.minutemind.enums.SessionType.WORK
        GROUP BY ws.userId
    """)
    List<Object[]> sumWorkMinutesForGoalByUsers(@Param("userIds") List<Long> userIds, @Param("goalId") Long goalId);
    @Query("SELECT ws FROM WorkSession ws WHERE ws.userId IN :userIds AND ws.endedAt IS NOT NULL AND ws.sessionType = com.be.minutemind.enums.SessionType.WORK ORDER BY ws.endedAt DESC")
    List<WorkSession> findRecentCompletedWorkSessionsByUserIds(@Param("userIds") List<Long> userIds, org.springframework.data.domain.Pageable pageable);
}

