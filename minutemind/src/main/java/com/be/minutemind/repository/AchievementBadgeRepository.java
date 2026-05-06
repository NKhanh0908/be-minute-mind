package com.be.minutemind.repository;

import com.be.minutemind.entities.AchievementBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AchievementBadgeRepository extends JpaRepository<AchievementBadge, Long> {
    Optional<AchievementBadge> findByCode(String code);
}
