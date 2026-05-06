package com.be.minutemind.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record GoalRequest(
        @NotBlank @Size(max = 255) String title,
        String description,
        @Size(max = 7) String color,
        Integer targetTotalMinutes,
        LocalDate deadline
) {
}
