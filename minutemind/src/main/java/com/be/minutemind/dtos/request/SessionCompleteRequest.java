package com.be.minutemind.dtos.request;

import jakarta.validation.constraints.PositiveOrZero;

public record SessionCompleteRequest(
        @PositiveOrZero Integer actualMinutes,
        boolean completedTask,
        String notes
) {
}
