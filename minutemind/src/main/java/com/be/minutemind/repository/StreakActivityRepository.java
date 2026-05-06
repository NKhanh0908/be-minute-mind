package com.be.minutemind.repository;

import com.be.minutemind.entities.StreakActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StreakActivityRepository extends JpaRepository<StreakActivity, Long> {
    Optional<StreakActivity> findByUserIdAndActivityDate(Long userId, LocalDate date);

    @Query("SELECT sa FROM StreakActivity sa WHERE sa.userId = :userId AND sa.activityDate >= :from ORDER BY sa.activityDate ASC")
    List<StreakActivity> findByUserIdFrom(@Param("userId") Long userId, @Param("from") LocalDate from);
}
