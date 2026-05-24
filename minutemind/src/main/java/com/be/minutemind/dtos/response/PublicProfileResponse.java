package com.be.minutemind.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicProfileResponse {
    private Long userId;
    private String name;
    private String avatarUrl;
    private boolean isFollowing;
    
    private int currentStreak;
    private int longestStreak;
    private int totalActiveDays;
    private int totalWorkMinutes;
    
    private List<BadgeResponse> badges;
}
