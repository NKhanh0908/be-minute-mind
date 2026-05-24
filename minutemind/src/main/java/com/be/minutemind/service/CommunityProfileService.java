package com.be.minutemind.service;

import com.be.minutemind.dtos.response.ActivityFeedResponse;
import com.be.minutemind.dtos.response.PublicProfileResponse;
import com.be.minutemind.dtos.response.UserSearchResponse;

import java.util.List;

public interface CommunityProfileService {
    List<UserSearchResponse> searchUsers(String keyword, Long currentUserId);
    PublicProfileResponse getPublicProfile(Long targetUserId, Long currentUserId);
    List<ActivityFeedResponse> getActivityFeed(Long currentUserId);
}
