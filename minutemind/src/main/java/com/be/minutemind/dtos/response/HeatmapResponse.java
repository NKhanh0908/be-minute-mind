package com.be.minutemind.dtos.response;

import java.time.LocalDate;

public record HeatmapResponse(
        LocalDate date,
        Integer totalMinutes
) {
}
