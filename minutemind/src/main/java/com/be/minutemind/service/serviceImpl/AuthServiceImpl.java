package com.be.minutemind.service.serviceImpl;

import com.be.minutemind.service.AuthService;
import com.be.minutemind.dtos.request.LoginRequest;
import com.be.minutemind.dtos.request.RefreshTokenRequest;
import com.be.minutemind.dtos.request.RegisterRequest;
import com.be.minutemind.dtos.response.AuthResponse;
import com.be.minutemind.dtos.response.UserResponse;
import com.be.minutemind.entities.RefreshToken;
import com.be.minutemind.entities.User;
import com.be.minutemind.exception.DuplicateResourceException;
import com.be.minutemind.exception.ValidationException;
import org.springframework.security.authentication.BadCredentialsException;
import com.be.minutemind.helper.JwtHelper;
import com.be.minutemind.mapper.UserMapper;
import com.be.minutemind.repository.RefreshTokenRepository;
import com.be.minutemind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtHelper jwtHelper;
    private final UserMapper userMapper;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiryMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .name(request.name())
                .isVerified(false)
                .isActive(true)
                .timezone("Asia/Ho_Chi_Minh")
                .streakThresholdMinutes(25)
                .build();

        user = userRepository.save(user);
        return generateTokenPair(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!user.getIsActive()) {
            throw new ValidationException("Account is deactivated");
        }

        return generateTokenPair(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.refreshToken());

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHash)
                .orElseThrow(() -> new BadCredentialsException("Invalid token"));

        if (refreshToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BadCredentialsException("Invalid token");
        }

        // Revoke old token
        refreshToken.setRevokedAt(OffsetDateTime.now());
        refreshTokenRepository.save(refreshToken);

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new BadCredentialsException("Invalid token"));

        return generateTokenPair(user);
    }

    @Transactional
    public void logout(Long userId, String rawRefreshToken) {
        if (rawRefreshToken != null && !rawRefreshToken.isEmpty()) {
            String hash = hashToken(rawRefreshToken);
            refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hash)
                    .ifPresent(token -> {
                        token.setRevokedAt(OffsetDateTime.now());
                        refreshTokenRepository.save(token);
                    });
        }
    }

    private AuthResponse generateTokenPair(User user) {
        String accessToken = jwtHelper.generateAccessToken(user);
        String rawRefreshToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(user.getId())
                .tokenHash(hashToken(rawRefreshToken))
                .expiresAt(OffsetDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .build();

        refreshTokenRepository.save(refreshToken);

        UserResponse userResponse = userMapper.toResponse(user);
        return new AuthResponse(accessToken, rawRefreshToken, userResponse);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
