package com.be.minutemind.controller;

import com.be.minutemind.annotation.CurrentUser;
import com.be.minutemind.dtos.request.InviteMemberRequest;
import com.be.minutemind.dtos.request.RespondInvitationRequest;
import com.be.minutemind.dtos.response.GoalInvitationResponse;
import com.be.minutemind.dtos.response.GoalMemberResponse;
import com.be.minutemind.dtos.response.LeaderboardResponse;
import com.be.minutemind.exception.ApiResponse;
import com.be.minutemind.service.LeaderboardService;
import com.be.minutemind.service.SharedGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
@Tag(name = "Community", description = "Community, Leaderboard & Shared Goal APIs")
public class CommunityController {

    private final LeaderboardService leaderboardService;
    private final SharedGoalService sharedGoalService;
    private final com.be.minutemind.service.CommunityProfileService communityProfileService;

    // =========================================================================
    // Leaderboard & Follow
    // =========================================================================

    @Operation(summary = "Get daily leaderboard", description = "Daily focus leaderboard including followed users")
    @GetMapping("/leaderboard/daily")
    public ResponseEntity<ApiResponse<List<LeaderboardResponse>>> getDailyLeaderboard(@CurrentUser Long userId) {
        return ResponseEntity.ok(ApiResponse.success(leaderboardService.getDailyFocusLeaderboard(userId, LocalDate.now())));
    }

    @Operation(summary = "Follow user")
    @PostMapping("/follow/{followingId}")
    public ResponseEntity<Void> followUser(@CurrentUser Long userId, @PathVariable Long followingId) {
        leaderboardService.followUser(userId, followingId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Unfollow user")
    @DeleteMapping("/unfollow/{followingId}")
    public ResponseEntity<Void> unfollowUser(@CurrentUser Long userId, @PathVariable Long followingId) {
        leaderboardService.unfollowUser(userId, followingId);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // Shared Goal — bật chia sẻ
    // =========================================================================

    @Operation(
        summary = "Enable shared mode for a goal",
        description = "Owner bật chế độ Shared cho goal. Idempotent — gọi nhiều lần vẫn an toàn."
    )
    @PostMapping("/goals/{goalId}/share")
    public ResponseEntity<Void> enableSharing(@CurrentUser Long userId, @PathVariable Long goalId) {
        sharedGoalService.enableSharing(userId, goalId);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // Shared Goal — quản lý thành viên (Owner)
    // =========================================================================

    @Operation(
        summary = "Invite a mutual friend to a shared goal",
        description = "Chỉ owner mới có thể mời. Hai bên phải follow lẫn nhau. Tối đa 10 thành viên."
    )
    @PostMapping("/goals/{goalId}/invitations")
    public ResponseEntity<Void> inviteMember(
            @CurrentUser Long userId,
            @PathVariable Long goalId,
            @RequestBody InviteMemberRequest request) {
        sharedGoalService.inviteMember(userId, goalId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Cancel a pending invitation",
        description = "Chỉ owner mới có thể huỷ lời mời đang PENDING."
    )
    @DeleteMapping("/goals/{goalId}/invitations/{invitationId}")
    public ResponseEntity<Void> cancelInvitation(
            @CurrentUser Long userId,
            @PathVariable Long goalId,
            @PathVariable Long invitationId) {
        sharedGoalService.cancelInvitation(userId, goalId, invitationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Kick a member from a shared goal",
        description = "Chỉ owner mới có thể kick. Không thể kick owner."
    )
    @DeleteMapping("/goals/{goalId}/members/{memberId}")
    public ResponseEntity<Void> kickMember(
            @CurrentUser Long userId,
            @PathVariable Long goalId,
            @PathVariable Long memberId) {
        sharedGoalService.kickMember(userId, goalId, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get pending invitations sent by owner for a goal",
        description = "Owner xem danh sách lời mời đang chờ trong goal của mình."
    )
    @GetMapping("/goals/{goalId}/invitations")
    public ResponseEntity<ApiResponse<List<GoalInvitationResponse>>> getPendingInvitationsForGoal(
            @CurrentUser Long userId,
            @PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(sharedGoalService.getPendingInvitationsForGoal(userId, goalId)));
    }

    // =========================================================================
    // Shared Goal — hành động của member
    // =========================================================================

    @Operation(
        summary = "Leave a shared goal",
        description = "Member tự rời goal. Owner không thể rời, chỉ Archive."
    )
    @DeleteMapping("/goals/{goalId}/members/me")
    public ResponseEntity<Void> leaveGoal(
            @CurrentUser Long userId,
            @PathVariable Long goalId) {
        sharedGoalService.leaveGoal(userId, goalId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Respond to a goal invitation",
        description = "Invitee chấp nhận (accept=true) hoặc từ chối (accept=false) lời mời."
    )
    @PatchMapping("/invitations/{invitationId}")
    public ResponseEntity<Void> respondToInvitation(
            @CurrentUser Long userId,
            @PathVariable Long invitationId,
            @RequestBody RespondInvitationRequest request) {
        sharedGoalService.respondToInvitation(userId, invitationId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get my pending invitations",
        description = "Xem tất cả lời mời đang chờ gửi tới tôi."
    )
    @GetMapping("/invitations")
    public ResponseEntity<ApiResponse<List<GoalInvitationResponse>>> getMyPendingInvitations(@CurrentUser Long userId) {
        return ResponseEntity.ok(ApiResponse.success(sharedGoalService.getPendingInvitations(userId)));
    }

    // =========================================================================
    // Shared Goal — xem tiến độ thành viên
    // =========================================================================

    @Operation(
        summary = "Get member progress board for a shared goal",
        description = "Xem bảng thành viên: avatar, phút hôm nay, tổng phút, % tiến độ."
    )
    @GetMapping("/goals/{goalId}/members")
    public ResponseEntity<ApiResponse<List<GoalMemberResponse>>> getMembers(
            @CurrentUser Long userId,
            @PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(sharedGoalService.getMembers(userId, goalId)));
    }

    // =========================================================================
    // Community Profile & Feed
    // =========================================================================

    @Operation(
        summary = "Search users",
        description = "Tìm kiếm user theo tên hoặc email"
    )
    @GetMapping("/users/search")
    public ResponseEntity<ApiResponse<List<com.be.minutemind.dtos.response.UserSearchResponse>>> searchUsers(
            @CurrentUser Long userId,
            @RequestParam("q") String query) {
        return ResponseEntity.ok(ApiResponse.success(communityProfileService.searchUsers(query, userId)));
    }

    @Operation(
        summary = "Get public profile",
        description = "Xem hồ sơ công khai của một user: streak, badges, tổng giờ"
    )
    @GetMapping("/users/{targetUserId}/profile")
    public ResponseEntity<ApiResponse<com.be.minutemind.dtos.response.PublicProfileResponse>> getPublicProfile(
            @CurrentUser Long userId,
            @PathVariable Long targetUserId) {
        return ResponseEntity.ok(ApiResponse.success(communityProfileService.getPublicProfile(targetUserId, userId)));
    }

    @Operation(
        summary = "Get activity feed",
        description = "Xem activity feed của những người mình follow"
    )
    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<List<com.be.minutemind.dtos.response.ActivityFeedResponse>>> getActivityFeed(
            @CurrentUser Long userId) {
        return ResponseEntity.ok(ApiResponse.success(communityProfileService.getActivityFeed(userId)));
    }
}
