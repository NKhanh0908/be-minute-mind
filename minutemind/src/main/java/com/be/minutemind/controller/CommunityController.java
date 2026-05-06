package com.be.minutemind.controller;

import com.be.minutemind.annotation.CurrentUser;
import com.be.minutemind.dtos.response.LeaderboardResponse;
import com.be.minutemind.exception.ApiResponse;
import com.be.minutemind.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
@Tag(name = "Community", description = "Community & Leaderboard APIs")
public class CommunityController {

    private final LeaderboardService leaderboardService;

    @Operation(summary = "Get daily leaderboard", description = "Get the daily focus leaderboard for the user and their followings", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Success")
    })
    @GetMapping("/leaderboard/daily")
    public ResponseEntity<ApiResponse<List<LeaderboardResponse>>> getDailyLeaderboard(@CurrentUser Long userId) {
        return ResponseEntity.ok(ApiResponse.success(leaderboardService.getDailyFocusLeaderboard(userId, LocalDate.now())));
    }

    @Operation(summary = "Follow user", description = "Follow another user to see them on leaderboard", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Follow successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/follow/{followingId}")
    public ResponseEntity<Void> followUser(@CurrentUser Long userId, @PathVariable Long followingId) {
        leaderboardService.followUser(userId, followingId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Unfollow user", description = "Unfollow a currently followed user", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Unfollow successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/unfollow/{followingId}")
    public ResponseEntity<Void> unfollowUser(@CurrentUser Long userId, @PathVariable Long followingId) {
        leaderboardService.unfollowUser(userId, followingId);
        return ResponseEntity.noContent().build();
    }
}
