package com.be.minutemind.repository;

import com.be.minutemind.entities.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUserId(Long userId);
    boolean existsByUserIdAndBadgeId(Long userId, Long badgeId);
    List<UserBadge> findByUserIdAndSeenFalse(Long userId);
}
