package com.be.minutemind.service.serviceImpl;

import com.be.minutemind.service.UpdateStreakService;
import com.be.minutemind.entities.Streak;
import com.be.minutemind.entities.StreakActivity;
import com.be.minutemind.entities.User;
import com.be.minutemind.repository.StreakActivityRepository;
import com.be.minutemind.repository.StreakRepository;
import com.be.minutemind.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class UpdateStreakServiceImpl implements UpdateStreakService {

    private final StreakRepository streakRepository;
    private final StreakActivityRepository streakActivityRepository;
    private final UserRepository userRepository;

    @Transactional
    public void updateStreak(Long userId, int addedMinutes) {
        LocalDate today = LocalDate.now();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return;

        StreakActivity activity = streakActivityRepository.findByUserIdAndActivityDate(userId, today)
                .orElse(StreakActivity.builder()
                        .userId(userId)
                        .activityDate(today)
                        .totalMinutes(0)
                        .isValid(false)
                        .build());

        activity.setTotalMinutes(activity.getTotalMinutes() + addedMinutes);

        boolean newlyValid = false;
        if (!activity.getIsValid() && activity.getTotalMinutes() >= user.getStreakThresholdMinutes()) {
            activity.setIsValid(true);
            newlyValid = true;
        }

        streakActivityRepository.save(activity);

        if (newlyValid) {
            updateStreakRecord(userId, today);
        }
    }

    private void updateStreakRecord(Long userId, LocalDate today) {
        Streak streak = streakRepository.findByUserId(userId)
                .orElse(Streak.builder()
                        .userId(userId)
                        .currentStreak(0)
                        .longestStreak(0)
                        .totalActiveDays(0)
                        .build());

        if (streak.getLastActiveDate() == null) {
            streak.increment(today);
        } else {
            long daysBetween = ChronoUnit.DAYS.between(streak.getLastActiveDate(), today);

            if (daysBetween == 1) {
                streak.increment(today);
            } else if (daysBetween > 1) {
                streak.reset(today);
            }
        }

        streakRepository.save(streak);
    }
}
