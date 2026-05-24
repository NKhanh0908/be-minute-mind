package com.be.minutemind.dtos.response;

import com.be.minutemind.enums.GoalMemberRole;

import java.time.OffsetDateTime;

public record GoalMemberResponse(
        Long userId,
        String name,
        String avatarUrl,
        GoalMemberRole role,
        OffsetDateTime joinedAt,
        // Thống kê tiến độ
        Integer todayMinutes,
        Integer totalMinutes,
        Integer progressPercent   // totalMinutes / targetTotalMinutes * 100, -1 nếu không có target
) {
}
