package com.be.minutemind.dtos.response;

import com.be.minutemind.enums.TaskStatus;

import java.time.OffsetDateTime;

public record TaskResponse(
        Long id,
        Long goalId,
        String title,
        String description,
        Integer estimatedMinutes,
        Integer totalLoggedMinutes,
        TaskStatus status,
        Integer sortOrder,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
