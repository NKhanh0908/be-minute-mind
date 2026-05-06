package com.be.minutemind.controller;

import com.be.minutemind.annotation.CurrentUser;
import com.be.minutemind.annotation.RateLimit;
import com.be.minutemind.dtos.request.LoginRequest;
import com.be.minutemind.dtos.request.RefreshTokenRequest;
import com.be.minutemind.dtos.request.RegisterRequest;
import com.be.minutemind.dtos.response.AuthResponse;
import com.be.minutemind.exception.ApiResponse;
import com.be.minutemind.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register user", description = "Register a new user in the system", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Registration successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    @PostMapping("/register")
    @RateLimit(requests = 10, perSeconds = 60)
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(authService.register(request)));
    }

    @Operation(summary = "Login user", description = "Authenticate a user and return tokens", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    @RateLimit(requests = 10, perSeconds = 60)
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request)));
    }

    @Operation(summary = "Refresh token", description = "Get a new access token using a refresh token", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PostMapping("/refresh")
    @RateLimit(requests = 10, perSeconds = 60)
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authService.refreshToken(request)));
    }

    @Operation(summary = "Logout user", description = "Logout user and invalidate refresh token", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Logout successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@CurrentUser Long userId, @RequestBody RefreshTokenRequest request) {
        authService.logout(userId, request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
