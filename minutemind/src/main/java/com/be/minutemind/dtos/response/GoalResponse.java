package com.be.minutemind.dtos.response;

import com.be.minutemind.enums.GoalStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record GoalResponse(
        Long id,
        String title,
        String description,
        String color,
        Integer targetTotalMinutes,
        LocalDate deadline,
        GoalStatus status,
        Integer sortOrder,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Integer totalLoggedMinutes
) {
}
