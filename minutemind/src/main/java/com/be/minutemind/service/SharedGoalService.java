package com.be.minutemind.service;

import com.be.minutemind.dtos.request.InviteMemberRequest;
import com.be.minutemind.dtos.request.RespondInvitationRequest;
import com.be.minutemind.dtos.response.GoalInvitationResponse;
import com.be.minutemind.dtos.response.GoalMemberResponse;

import java.util.List;

public interface SharedGoalService {

    // --- Bật/tắt Shared mode ---
    void enableSharing(Long userId, Long goalId);

    // --- Quản lý thành viên ---
    void inviteMember(Long ownerId, Long goalId, InviteMemberRequest request);
    void cancelInvitation(Long ownerId, Long goalId, Long invitationId);
    void kickMember(Long ownerId, Long goalId, Long memberId);
    void leaveGoal(Long memberId, Long goalId);

    // --- Phản hồi lời mời ---
    void respondToInvitation(Long userId, Long invitationId, RespondInvitationRequest request);

    // --- Xem dữ liệu ---
    List<GoalMemberResponse> getMembers(Long userId, Long goalId);
    List<GoalInvitationResponse> getPendingInvitations(Long userId);
    List<GoalInvitationResponse> getPendingInvitationsForGoal(Long userId, Long goalId);
}
