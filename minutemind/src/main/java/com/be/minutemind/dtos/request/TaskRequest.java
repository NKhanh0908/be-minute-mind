package com.be.minutemind.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskRequest(
        @NotNull Long goalId,
        @NotBlank @Size(max = 255) String title,
        String description,
        Integer estimatedMinutes
) {
}
