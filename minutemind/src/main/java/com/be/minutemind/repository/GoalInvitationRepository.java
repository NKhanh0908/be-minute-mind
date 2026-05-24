package com.be.minutemind.repository;

import com.be.minutemind.entities.GoalInvitation;
import com.be.minutemind.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalInvitationRepository extends JpaRepository<GoalInvitation, Long> {

    Optional<GoalInvitation> findByGoalIdAndInviteeId(Long goalId, Long inviteeId);

    Optional<GoalInvitation> findByIdAndInviteeId(Long id, Long inviteeId);

    Optional<GoalInvitation> findByIdAndGoalId(Long id, Long goalId);

    // Lời mời đang chờ gửi tới user (để hiển thị inbox)
    @Query("""
        SELECT gi FROM GoalInvitation gi
        WHERE gi.inviteeId = :inviteeId AND gi.status = com.be.minutemind.enums.InvitationStatus.PENDING
        ORDER BY gi.createdAt DESC
    """)
    List<GoalInvitation> findPendingByInviteeId(@Param("inviteeId") Long inviteeId);

    // Lời mời đang chờ trong 1 goal cụ thể (để owner xem)
    @Query("""
        SELECT gi FROM GoalInvitation gi
        WHERE gi.goalId = :goalId AND gi.status = com.be.minutemind.enums.InvitationStatus.PENDING
        ORDER BY gi.createdAt DESC
    """)
    List<GoalInvitation> findPendingByGoalId(@Param("goalId") Long goalId);

    boolean existsByGoalIdAndInviteeIdAndStatus(Long goalId, Long inviteeId, InvitationStatus status);
}
