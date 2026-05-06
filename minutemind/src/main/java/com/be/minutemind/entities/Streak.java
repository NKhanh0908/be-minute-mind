package com.be.minutemind.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "streaks")
public class Streak {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    @Column(nullable = false)
    @Builder.Default
    private Integer currentStreak = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer longestStreak = 0;

    private LocalDate lastActiveDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer totalActiveDays = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private User user;

    public void increment(LocalDate today) {
        currentStreak++;
        if (currentStreak > longestStreak) longestStreak = currentStreak;
        lastActiveDate = today;
        totalActiveDays++;
    }

    public void reset(LocalDate today) {
        currentStreak = 1;
        if (longestStreak < 1) longestStreak = 1;
        lastActiveDate = today;
        totalActiveDays++;
    }
}
