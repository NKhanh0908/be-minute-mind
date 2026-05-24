package com.be.minutemind.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Name không được để trống")
        @Size(min = 2, max = 100, message = "Name phải từ 2 đến 100 ký tự")
        String name,

        String timezone,

        Integer streakThresholdMinutes
) {
}
