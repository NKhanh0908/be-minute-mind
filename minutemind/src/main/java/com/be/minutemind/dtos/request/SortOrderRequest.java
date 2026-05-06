package com.be.minutemind.dtos.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SortOrderRequest(
        @NotEmpty List<Long> ids
) {
}
