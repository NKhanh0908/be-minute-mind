package com.be.minutemind.repository;

import com.be.minutemind.entities.Goal;
import com.be.minutemind.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUserIdAndDeletedAtIsNullOrderBySortOrderAsc(Long userId);

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.userId = :uid AND g.status = :status AND g.deletedAt IS NULL")
    long countByUserIdAndStatusAndDeletedAtIsNull(@Param("uid") Long userId, @Param("status") GoalStatus status);
}
