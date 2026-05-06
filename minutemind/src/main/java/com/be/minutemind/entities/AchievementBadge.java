package com.be.minutemind.entities;

import com.be.minutemind.enums.BadgeConditionType;
import com.be.minutemind.enums.BadgeRarity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "achievement_badges")
public class AchievementBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    private String description;

    @Column(length = 10)
    private String icon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BadgeRarity rarity = BadgeRarity.COMMON;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeConditionType conditionType;

    @Column(nullable = false)
    private Integer conditionValue;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
