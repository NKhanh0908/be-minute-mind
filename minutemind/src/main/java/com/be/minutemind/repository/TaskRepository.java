package com.be.minutemind.repository;

import com.be.minutemind.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByGoalIdAndDeletedAtIsNullOrderBySortOrderAsc(Long goalId);

    @Query("SELECT COALESCE(SUM(t.totalLoggedMinutes), 0) FROM Task t WHERE t.goalId = :goalId AND t.deletedAt IS NULL")
    Integer sumLoggedMinutesByGoalId(@Param("goalId") Long goalId);

    @Query("SELECT t.goalId, COALESCE(SUM(t.totalLoggedMinutes), 0) FROM Task t " +
           "WHERE t.goalId IN :goalIds AND t.deletedAt IS NULL GROUP BY t.goalId")
    List<Object[]> sumLoggedMinutesByGoalIds(@Param("goalIds") List<Long> goalIds);

    // Atomic UPDATE
    @Modifying
    @Query("UPDATE Task t SET t.totalLoggedMinutes = t.totalLoggedMinutes + :minutes WHERE t.id = :id")
    void addLoggedMinutes(@Param("id") Long taskId, @Param("minutes") int minutes);
}
