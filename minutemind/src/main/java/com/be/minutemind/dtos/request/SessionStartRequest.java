package com.be.minutemind.dtos.request;

import com.be.minutemind.enums.SessionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SessionStartRequest(
        @NotNull Long taskId,
        @NotNull SessionType sessionType,
        @NotNull @Positive Integer plannedMinutes
) {
}
