package com.be.minutemind.service.serviceImpl;

import com.be.minutemind.dtos.request.InviteMemberRequest;
import com.be.minutemind.dtos.request.RespondInvitationRequest;
import com.be.minutemind.dtos.response.GoalInvitationResponse;
import com.be.minutemind.dtos.response.GoalMemberResponse;
import com.be.minutemind.entities.*;
import com.be.minutemind.enums.GoalMemberRole;
import com.be.minutemind.enums.InvitationStatus;
import com.be.minutemind.enums.NotificationType;
import com.be.minutemind.enums.SessionType;
import com.be.minutemind.exception.ResourceNotFoundException;
import com.be.minutemind.exception.ValidationException;
import com.be.minutemind.repository.*;
import com.be.minutemind.service.SharedGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SharedGoalServiceImpl implements SharedGoalService {

    private static final int MAX_MEMBERS = 10;

    private final GoalRepository goalRepository;
    private final GoalMemberRepository goalMemberRepository;
    private final GoalInvitationRepository goalInvitationRepository;
    private final UserRepository userRepository;
    private final UserRelationshipRepository userRelationshipRepository;
    private final NotificationRepository notificationRepository;
    private final WorkSessionRepository workSessionRepository;

    // =========================================================================
    // Bật Shared mode
    // =========================================================================

    @Override
    @Transactional
    public void enableSharing(Long userId, Long goalId) {
        Goal goal = getGoalAsOwner(userId, goalId);
        if (Boolean.TRUE.equals(goal.getIsShared())) {
            return; // already shared — idempotent
        }
        goal.setIsShared(true);
        goalRepository.save(goal);

        // Tự động thêm owner vào goal_members nếu chưa có
        if (!goalMemberRepository.existsByGoalIdAndUserId(goalId, userId)) {
            GoalMember ownerMember = GoalMember.builder()
                    .goalId(goalId)
                    .userId(userId)
                    .role(GoalMemberRole.OWNER)
                    .build();
            goalMemberRepository.save(ownerMember);
        }
    }

    // =========================================================================
    // Mời thành viên
    // =========================================================================

    @Override
    @Transactional
    public void inviteMember(Long ownerId, Long goalId, InviteMemberRequest request) {
        Goal goal = getGoalAsOwner(ownerId, goalId);

        if (!Boolean.TRUE.equals(goal.getIsShared())) {
            throw new ValidationException("Goal chưa bật chế độ Shared. Hãy bật Shared trước.");
        }

        Long inviteeId = request.inviteeId();
        if (ownerId.equals(inviteeId)) {
            throw new ValidationException("Không thể tự mời chính mình.");
        }

        // Kiểm tra invitee tồn tại
        User invitee = userRepository.findById(inviteeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Chỉ mời bạn bè (follow lẫn nhau)
        boolean ownerFollowsInvitee = userRelationshipRepository.existsByFollowerIdAndFollowingId(ownerId, inviteeId);
        boolean inviteeFollowsOwner = userRelationshipRepository.existsByFollowerIdAndFollowingId(inviteeId, ownerId);
        if (!ownerFollowsInvitee || !inviteeFollowsOwner) {
            throw new ValidationException("Chỉ có thể mời người dùng đang follow lẫn nhau.");
        }

        // Kiểm tra đã là thành viên chưa
        if (goalMemberRepository.existsByGoalIdAndUserId(goalId, inviteeId)) {
            throw new ValidationException("Người dùng đã là thành viên của goal này.");
        }

        // Kiểm tra đã có lời mời PENDING chưa
        if (goalInvitationRepository.existsByGoalIdAndInviteeIdAndStatus(goalId, inviteeId, InvitationStatus.PENDING)) {
            throw new ValidationException("Đã có lời mời đang chờ đối với người dùng này.");
        }

        // Kiểm tra giới hạn thành viên (kể cả pending invitations)
        long currentMembers = goalMemberRepository.countByGoalId(goalId);
        long pendingInvitations = goalInvitationRepository.findPendingByGoalId(goalId).size();
        if (currentMembers + pendingInvitations >= MAX_MEMBERS) {
            throw new ValidationException("Goal đã đạt giới hạn " + MAX_MEMBERS + " thành viên.");
        }

        // Tạo invitation
        GoalInvitation invitation = GoalInvitation.builder()
                .goalId(goalId)
                .inviterId(ownerId)
                .inviteeId(inviteeId)
                .status(InvitationStatus.PENDING)
                .build();
        goalInvitationRepository.save(invitation);

        // Gửi notification cho invitee
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));
        Notification notification = Notification.builder()
                .userId(inviteeId)
                .type(NotificationType.FRIEND_MILESTONE)
                .title("Lời mời tham gia Goal Chung")
                .body(owner.getName() + " đã mời bạn tham gia goal: \"" + goal.getTitle() + "\"")
                .payload("{\"invitationId\":" + invitation.getId() + ",\"goalId\":" + goalId + "}")
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    // =========================================================================
    // Huỷ lời mời (owner)
    // =========================================================================

    @Override
    @Transactional
    public void cancelInvitation(Long ownerId, Long goalId, Long invitationId) {
        getGoalAsOwner(ownerId, goalId);

        GoalInvitation invitation = goalInvitationRepository.findByIdAndGoalId(invitationId, goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ValidationException("Chỉ có thể huỷ lời mời đang ở trạng thái PENDING.");
        }

        invitation.setStatus(InvitationStatus.CANCELLED);
        goalInvitationRepository.save(invitation);
    }

    // =========================================================================
    // Kick thành viên (owner)
    // =========================================================================

    @Override
    @Transactional
    public void kickMember(Long ownerId, Long goalId, Long memberId) {
        getGoalAsOwner(ownerId, goalId);

        if (ownerId.equals(memberId)) {
            throw new ValidationException("Owner không thể kick chính mình. Hãy Archive goal nếu muốn kết thúc.");
        }

        GoalMember member = goalMemberRepository.findByGoalIdAndUserId(goalId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in this goal"));

        if (member.getRole() == GoalMemberRole.OWNER) {
            throw new ValidationException("Không thể kick OWNER.");
        }

        goalMemberRepository.delete(member);
    }

    // =========================================================================
    // Rời goal (member tự rời)
    // =========================================================================

    @Override
    @Transactional
    public void leaveGoal(Long memberId, Long goalId) {
        goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        GoalMember member = goalMemberRepository.findByGoalIdAndUserId(goalId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Bạn không phải là thành viên của goal này."));

        if (member.getRole() == GoalMemberRole.OWNER) {
            throw new ValidationException("Owner không thể rời goal. Hãy Archive goal nếu muốn kết thúc.");
        }

        goalMemberRepository.delete(member);
    }

    // =========================================================================
    // Phản hồi lời mời (invitee)
    // =========================================================================

    @Override
    @Transactional
    public void respondToInvitation(Long userId, Long invitationId, RespondInvitationRequest request) {
        GoalInvitation invitation = goalInvitationRepository.findByIdAndInviteeId(invitationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ValidationException("Lời mời này không còn ở trạng thái PENDING.");
        }

        invitation.setRespondedAt(OffsetDateTime.now());

        if (request.accept()) {
            // Kiểm tra lại giới hạn thành viên trước khi chấp nhận
            long currentMembers = goalMemberRepository.countByGoalId(invitation.getGoalId());
            if (currentMembers >= MAX_MEMBERS) {
                invitation.setStatus(InvitationStatus.DECLINED);
                goalInvitationRepository.save(invitation);
                throw new ValidationException("Goal đã đạt giới hạn thành viên. Lời mời bị từ chối tự động.");
            }

            invitation.setStatus(InvitationStatus.ACCEPTED);
            goalInvitationRepository.save(invitation);

            GoalMember newMember = GoalMember.builder()
                    .goalId(invitation.getGoalId())
                    .userId(userId)
                    .role(GoalMemberRole.MEMBER)
                    .build();
            goalMemberRepository.save(newMember);
        } else {
            invitation.setStatus(InvitationStatus.DECLINED);
            goalInvitationRepository.save(invitation);
        }
    }

    // =========================================================================
    // Xem danh sách thành viên + tiến độ
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<GoalMemberResponse> getMembers(Long userId, Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        // Phải là thành viên hoặc owner mới xem được
        if (!goalMemberRepository.existsByGoalIdAndUserId(goalId, userId)) {
            throw new ValidationException("Bạn không có quyền xem thành viên của goal này.");
        }

        List<GoalMember> members = goalMemberRepository.findByGoalId(goalId);
        List<Long> memberUserIds = members.stream().map(GoalMember::getUserId).toList();

        // Fetch tất cả user info trong 1 query
        Map<Long, User> userMap = userRepository.findAllById(memberUserIds)
                .stream().collect(Collectors.toMap(User::getId, u -> u));

        // Tính phút hôm nay cho từng member
        OffsetDateTime startOfToday = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime startOfTomorrow = startOfToday.plusDays(1);

        Map<Long, Integer> todayMinutesMap = workSessionRepository
                .sumWorkMinutesForDateByUsers(memberUserIds, startOfToday, startOfTomorrow)
                .stream().collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()));

        // Tính tổng phút của từng member trong goal này
        Map<Long, Integer> totalMinutesMap = workSessionRepository
                .sumWorkMinutesForGoalByUsers(memberUserIds, goalId)
                .stream().collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()));

        return members.stream().map(member -> {
            User u = userMap.get(member.getUserId());
            int todayMin = todayMinutesMap.getOrDefault(member.getUserId(), 0);
            int totalMin = totalMinutesMap.getOrDefault(member.getUserId(), 0);
            int progress = -1;
            if (goal.getTargetTotalMinutes() != null && goal.getTargetTotalMinutes() > 0) {
                progress = Math.min(100, (int) Math.round(totalMin * 100.0 / goal.getTargetTotalMinutes()));
            }
            return new GoalMemberResponse(
                    member.getUserId(),
                    u != null ? u.getName() : "Unknown",
                    u != null ? u.getAvatarUrl() : null,
                    member.getRole(),
                    member.getJoinedAt(),
                    todayMin,
                    totalMin,
                    progress
            );
        }).toList();
    }

    // =========================================================================
    // Xem lời mời đang chờ của tôi (inbox)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<GoalInvitationResponse> getPendingInvitations(Long userId) {
        List<GoalInvitation> invitations = goalInvitationRepository.findPendingByInviteeId(userId);
        return invitations.stream().map(this::toInvitationResponse).toList();
    }

    // =========================================================================
    // Xem lời mời đang chờ trong 1 goal (owner xem)
    // =========================================================================

    @Override
    @Transactional(readOnly = true)
    public List<GoalInvitationResponse> getPendingInvitationsForGoal(Long userId, Long goalId) {
        getGoalAsOwner(userId, goalId);
        List<GoalInvitation> invitations = goalInvitationRepository.findPendingByGoalId(goalId);
        return invitations.stream().map(this::toInvitationResponse).toList();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Goal getGoalAsOwner(Long userId, Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUserId().equals(userId) || goal.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Goal not found");
        }
        return goal;
    }

    private GoalInvitationResponse toInvitationResponse(GoalInvitation inv) {
        // Lazy-load các thực thể liên quan — đã trong transaction
        Goal goal = inv.getGoal();
        User inviter = inv.getInviter();
        User invitee = inv.getInvitee();

        // Fallback nếu lazy load không hoạt động (ví dụ entity detached)
        if (goal == null) goal = goalRepository.findById(inv.getGoalId()).orElse(null);
        if (inviter == null) inviter = userRepository.findById(inv.getInviterId()).orElse(null);
        if (invitee == null) invitee = userRepository.findById(inv.getInviteeId()).orElse(null);

        final Goal g = goal;
        final User ir = inviter;
        final User ie = invitee;

        return new GoalInvitationResponse(
                inv.getId(),
                inv.getGoalId(),
                g != null ? g.getTitle() : null,
                inv.getInviterId(),
                ir != null ? ir.getName() : null,
                ir != null ? ir.getAvatarUrl() : null,
                inv.getInviteeId(),
                ie != null ? ie.getName() : null,
                inv.getStatus(),
                inv.getCreatedAt(),
                inv.getRespondedAt()
        );
    }
}
