package com.be.minutemind.service.serviceImpl;

import com.be.minutemind.dtos.response.ActivityFeedResponse;
import com.be.minutemind.dtos.response.BadgeResponse;
import com.be.minutemind.dtos.response.PublicProfileResponse;
import com.be.minutemind.dtos.response.UserSearchResponse;
import com.be.minutemind.entities.Streak;
import com.be.minutemind.entities.User;
import com.be.minutemind.entities.UserBadge;
import com.be.minutemind.entities.WorkSession;
import com.be.minutemind.exception.ResourceNotFoundException;
import com.be.minutemind.repository.StreakRepository;
import com.be.minutemind.repository.UserBadgeRepository;
import com.be.minutemind.repository.UserRelationshipRepository;
import com.be.minutemind.repository.UserRepository;
import com.be.minutemind.repository.WorkSessionRepository;
import com.be.minutemind.service.CommunityProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityProfileServiceImpl implements CommunityProfileService {

    private final UserRepository userRepository;
    private final UserRelationshipRepository userRelationshipRepository;
    private final StreakRepository streakRepository;
    private final WorkSessionRepository workSessionRepository;
    private final UserBadgeRepository userBadgeRepository;

    @Override
    public List<UserSearchResponse> searchUsers(String keyword, Long currentUserId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        List<User> users = userRepository.searchByKeyword(keyword.trim(), PageRequest.of(0, 10));
        List<Long> followingIds = userRelationshipRepository.findFollowingIdsByFollowerId(currentUserId);
        
        return users.stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .map(u -> UserSearchResponse.builder()
                        .userId(u.getId())
                        .name(u.getName())
                        .avatarUrl(u.getAvatarUrl())
                        .isFollowing(followingIds.contains(u.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public PublicProfileResponse getPublicProfile(Long targetUserId, Long currentUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        boolean isFollowing = userRelationshipRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId);
        
        Streak streak = streakRepository.findByUserId(targetUserId).orElse(null);
        int currentStreak = streak != null ? streak.getCurrentStreak() : 0;
        int longestStreak = streak != null ? streak.getLongestStreak() : 0;
        int totalActiveDays = streak != null ? streak.getTotalActiveDays() : 0;
        
        Integer totalWorkMinutes = workSessionRepository.sumTotalWorkMinutes(targetUserId);
        if (totalWorkMinutes == null) totalWorkMinutes = 0;
        
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(targetUserId);
        List<BadgeResponse> badgeResponses = userBadges.stream()
                .map(ub -> BadgeResponse.builder()
                        .code(ub.getBadge().getCode())
                        .name(ub.getBadge().getName())
                        .icon(ub.getBadge().getIcon())
                        .rarity(ub.getBadge().getRarity().name())
                        .build())
                .collect(Collectors.toList());
                
        return PublicProfileResponse.builder()
                .userId(targetUser.getId())
                .name(targetUser.getName())
                .avatarUrl(targetUser.getAvatarUrl())
                .isFollowing(isFollowing)
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .totalActiveDays(totalActiveDays)
                .totalWorkMinutes(totalWorkMinutes)
                .badges(badgeResponses)
                .build();
    }

    @Override
    public List<ActivityFeedResponse> getActivityFeed(Long currentUserId) {
        List<Long> followingIds = userRelationshipRepository.findFollowingIdsByFollowerId(currentUserId);
        if (followingIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<WorkSession> recentSessions = workSessionRepository.findRecentCompletedWorkSessionsByUserIds(followingIds, PageRequest.of(0, 20));
        
        List<User> users = userRepository.findAllById(followingIds);
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        
        return recentSessions.stream().map(ws -> {
            User user = userMap.get(ws.getUserId());
            String userName = user != null ? user.getName() : "Người dùng";
            String avatarUrl = user != null ? user.getAvatarUrl() : null;
            
            String content = "đã hoàn thành " + ws.getActualMinutes() + " phút tập trung";
            
            return ActivityFeedResponse.builder()
                    .id(ws.getId())
                    .userId(ws.getUserId())
                    .userName(userName)
                    .userAvatarUrl(avatarUrl)
                    .type("WORK_SESSION")
                    .timestamp(ws.getEndedAt())
                    .content(content)
                    .build();
        }).collect(Collectors.toList());
    }
}
