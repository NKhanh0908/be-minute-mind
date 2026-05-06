package com.be.minutemind.service.serviceImpl;

import com.be.minutemind.service.StatsService;
import com.be.minutemind.dtos.response.HeatmapResponse;
import com.be.minutemind.dtos.response.StatsSummaryResponse;
import com.be.minutemind.entities.Streak;
import com.be.minutemind.entities.StreakActivity;
import com.be.minutemind.repository.StreakActivityRepository;
import com.be.minutemind.repository.StreakRepository;
import com.be.minutemind.repository.WorkSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final WorkSessionRepository workSessionRepository;
    private final StreakRepository streakRepository;
    private final StreakActivityRepository streakActivityRepository;

    @Transactional(readOnly = true)
    public StatsSummaryResponse getSummary(Long userId, String timezone) {
        ZoneId zoneId = ZoneId.of(timezone != null ? timezone : "Asia/Ho_Chi_Minh");

        OffsetDateTime startOfDay = LocalDate.now(zoneId).atStartOfDay(zoneId).toOffsetDateTime();
        OffsetDateTime endOfDay = startOfDay.plusDays(1);

        Integer todayMinutes = workSessionRepository.sumWorkMinutesForDate(userId, startOfDay, endOfDay);
        Integer totalMinutes = workSessionRepository.sumTotalWorkMinutes(userId);

        Streak streak = streakRepository.findByUserId(userId).orElse(null);
        Integer currentStreak = streak != null ? streak.getCurrentStreak() : 0;
        Integer longestStreak = streak != null ? streak.getLongestStreak() : 0;
        Integer totalActiveDays = streak != null ? streak.getTotalActiveDays() : 0;

        if (streak != null && streak.getLastActiveDate() != null) {
            LocalDate today = LocalDate.now(zoneId);
            long daysSinceLastActive = java.time.temporal.ChronoUnit.DAYS.between(streak.getLastActiveDate(), today);
            if (daysSinceLastActive > 1) {
                currentStreak = 0;
            }
        }

        return new StatsSummaryResponse(
                todayMinutes,
                totalMinutes,
                currentStreak,
                longestStreak,
                totalActiveDays);
    }

    @Transactional(readOnly = true)
    public List<HeatmapResponse> getHeatmap(Long userId, int days) {
        LocalDate fromDate = LocalDate.now().minusDays(days);
        List<StreakActivity> activities = streakActivityRepository.findByUserIdFrom(userId, fromDate);

        return activities.stream()
                .map(a -> new HeatmapResponse(a.getActivityDate(), a.getTotalMinutes()))
                .collect(Collectors.toList());
    }
}
