package com.be.minutemind.dtos.response;

public record StatsSummaryResponse(
        Integer todayMinutes,
        Integer totalMinutes,
        Integer currentStreak,
        Integer longestStreak,
        Integer totalActiveDays
) {
}
