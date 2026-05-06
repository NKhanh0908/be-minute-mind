package com.be.minutemind.repository;

import com.be.minutemind.entities.GoalProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoalProgressRepository extends JpaRepository<GoalProgress, Long> {
    Optional<GoalProgress> findByGoalIdAndTrackedDate(Long goalId, LocalDate date);

    @Query("SELECT gp FROM GoalProgress gp WHERE gp.userId = :userId AND gp.trackedDate >= :from ORDER BY gp.trackedDate")
    List<GoalProgress> findByUserIdFrom(@Param("userId") Long userId, @Param("from") LocalDate from);
}
