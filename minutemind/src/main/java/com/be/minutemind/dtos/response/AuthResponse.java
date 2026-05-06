package com.be.minutemind.dtos.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
}
