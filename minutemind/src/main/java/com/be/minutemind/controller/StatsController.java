package com.be.minutemind.controller;

import com.be.minutemind.annotation.CurrentUser;
import com.be.minutemind.dtos.response.HeatmapResponse;
import com.be.minutemind.dtos.response.StatsSummaryResponse;
import com.be.minutemind.exception.ApiResponse;
import com.be.minutemind.service.StatsService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "User Statistics APIs")
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "Get stats summary", description = "Retrieve a summary of user focus statistics", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stats retrieved", content = @Content(schema = @Schema(implementation = StatsSummaryResponse.class)))
    })
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<StatsSummaryResponse>> getSummary(@CurrentUser Long userId, @RequestParam(defaultValue = "Asia/Ho_Chi_Minh") String timezone) {
        return ResponseEntity.ok(ApiResponse.success(statsService.getSummary(userId, timezone)));
    }

    @Operation(summary = "Get heatmap data", description = "Retrieve heatmap data for the past specified days", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Heatmap data retrieved")
    })
    @GetMapping("/heatmap")
    public ResponseEntity<ApiResponse<List<HeatmapResponse>>> getHeatmap(@CurrentUser Long userId, @RequestParam(defaultValue = "365") int days) {
        return ResponseEntity.ok(ApiResponse.success(statsService.getHeatmap(userId, days)));
    }
}
