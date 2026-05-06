package com.be.minutemind.dtos.response;

public record UserResponse(
        Long id,
        String email,
        String name,
        String avatarUrl,
        String timezone,
        Integer streakThresholdMinutes
) {
}
