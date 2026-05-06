package com.be.minutemind.service.serviceImpl;

import com.be.minutemind.service.CheckBadgeService;
import com.be.minutemind.entities.AchievementBadge;
import com.be.minutemind.entities.UserBadge;
import com.be.minutemind.enums.BadgeConditionType;
import com.be.minutemind.repository.AchievementBadgeRepository;
import com.be.minutemind.repository.StreakRepository;
import com.be.minutemind.repository.UserBadgeRepository;
import com.be.minutemind.repository.WorkSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckBadgeServiceImpl implements CheckBadgeService {

    private final AchievementBadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final WorkSessionRepository workSessionRepository;
    private final StreakRepository streakRepository;

    @Transactional
    public void checkAndAwardBadges(Long userId) {
        List<AchievementBadge> allBadges = badgeRepository.findAll();

        Integer totalFocusMinutes = workSessionRepository.sumTotalWorkMinutes(userId);
        int currentStreak = streakRepository.findByUserId(userId)
                .map(s -> s.getCurrentStreak())
                .orElse(0);

        for (AchievementBadge badge : allBadges) {
            if (userBadgeRepository.existsByUserIdAndBadgeId(userId, badge.getId())) {
                continue;
            }

            boolean conditionMet = false;

            if (badge.getConditionType() == BadgeConditionType.TOTAL_MINUTES) {
                conditionMet = totalFocusMinutes >= badge.getConditionValue();
            } else if (badge.getConditionType() == BadgeConditionType.STREAK_DAYS) {
                conditionMet = currentStreak >= badge.getConditionValue();
            }

            if (conditionMet) {
                awardBadge(userId, badge);
            }
        }
    }

    private void awardBadge(Long userId, AchievementBadge badge) {
        UserBadge userBadge = UserBadge.builder()
                .userId(userId)
                .badgeId(badge.getId())
                .earnedAt(OffsetDateTime.now())
                .seen(false)
                .build();
        userBadgeRepository.save(userBadge);
    }
}
