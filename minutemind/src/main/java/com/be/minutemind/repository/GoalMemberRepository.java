package com.be.minutemind.repository;

import com.be.minutemind.entities.GoalMember;
import com.be.minutemind.enums.GoalMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalMemberRepository extends JpaRepository<GoalMember, Long> {

    List<GoalMember> findByGoalId(Long goalId);

    Optional<GoalMember> findByGoalIdAndUserId(Long goalId, Long userId);

    boolean existsByGoalIdAndUserId(Long goalId, Long userId);

    long countByGoalId(Long goalId);

    @Query("SELECT gm FROM GoalMember gm WHERE gm.userId = :userId")
    List<GoalMember> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM GoalMember gm WHERE gm.goalId = :goalId AND gm.userId = :userId AND gm.role = com.be.minutemind.enums.GoalMemberRole.MEMBER")
    void deleteMember(@Param("goalId") Long goalId, @Param("userId") Long userId);

    @Query("SELECT gm.userId FROM GoalMember gm WHERE gm.goalId = :goalId")
    List<Long> findMemberUserIdsByGoalId(@Param("goalId") Long goalId);
}
