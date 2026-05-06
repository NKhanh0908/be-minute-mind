package com.be.minutemind.service;

import com.be.minutemind.dtos.request.LoginRequest;
import com.be.minutemind.dtos.request.RefreshTokenRequest;
import com.be.minutemind.dtos.request.RegisterRequest;
import com.be.minutemind.dtos.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(Long userId, String rawRefreshToken);
}
