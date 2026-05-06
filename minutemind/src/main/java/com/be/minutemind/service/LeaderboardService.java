package com.be.minutemind.service;

import com.be.minutemind.dtos.response.LeaderboardResponse;

import java.time.LocalDate;
import java.util.List;

public interface LeaderboardService {
    List<LeaderboardResponse> getDailyFocusLeaderboard(Long userId, LocalDate date);
    void followUser(Long followerId, Long followingId);
    void unfollowUser(Long followerId, Long followingId);
}
