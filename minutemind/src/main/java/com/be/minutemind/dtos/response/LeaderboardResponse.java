package com.be.minutemind.dtos.response;

public record LeaderboardResponse(
        Long userId,
        String name,
        String avatarUrl,
        Integer value,
        Integer rank
) {
}
