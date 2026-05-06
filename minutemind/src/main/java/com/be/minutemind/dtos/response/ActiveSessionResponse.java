package com.be.minutemind.dtos.response;

import com.be.minutemind.enums.SessionType;

import java.time.OffsetDateTime;

public record ActiveSessionResponse(
        Long sessionId,
        Long taskId,
        SessionType sessionType,
        Integer plannedMinutes,
        OffsetDateTime startedAt,
        OffsetDateTime lastHeartbeatAt
) {
}
