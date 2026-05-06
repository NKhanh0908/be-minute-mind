package com.be.minutemind.service.serviceImpl;

import com.be.minutemind.service.LeaderboardService;
import com.be.minutemind.dtos.response.LeaderboardResponse;
import com.be.minutemind.entities.UserRelationship;
import com.be.minutemind.repository.StreakRepository;
import com.be.minutemind.repository.UserRelationshipRepository;
import com.be.minutemind.repository.UserRepository;
import com.be.minutemind.repository.WorkSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {

    private final UserRelationshipRepository userRelationshipRepository;
    private final WorkSessionRepository workSessionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<LeaderboardResponse> getDailyFocusLeaderboard(Long userId, LocalDate date) {
        List<Long> followingIds = userRelationshipRepository.findFollowingIdsByFollowerId(userId);
        followingIds.add(userId);

        List<Object[]> userMinutes = workSessionRepository.sumWorkMinutesForDateByUsers(
                followingIds,
                date.atStartOfDay(java.time.ZoneId.of("UTC")).toOffsetDateTime(),
                date.plusDays(1).atStartOfDay(java.time.ZoneId.of("UTC")).toOffsetDateTime());

        Map<Long, Integer> minutesMap = userMinutes.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).intValue()));

        for (Long uid : followingIds) {
            minutesMap.putIfAbsent(uid, 0);
        }

        List<LeaderboardResponse> leaderboard = new ArrayList<>();
        minutesMap.forEach((uid, minutes) -> {
            userRepository.findById(uid).ifPresent(user -> {
                leaderboard.add(new LeaderboardResponse(
                        user.getId(),
                        user.getName(),
                        user.getAvatarUrl(),
                        minutes,
                        0));
            });
        });

        leaderboard.sort(Comparator.comparing(LeaderboardResponse::value).reversed()
                .thenComparing(LeaderboardResponse::name));

        for (int i = 0; i < leaderboard.size(); i++) {
            LeaderboardResponse current = leaderboard.get(i);
            leaderboard.set(i, new LeaderboardResponse(
                    current.userId(), current.name(), current.avatarUrl(), current.value(), i + 1));
        }

        return leaderboard;
    }

    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId))
            return;

        if (!userRelationshipRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            UserRelationship relationship = UserRelationship.builder()
                    .followerId(followerId)
                    .followingId(followingId)
                    .build();
            userRelationshipRepository.save(relationship);
        }
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        userRelationshipRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    }
}
