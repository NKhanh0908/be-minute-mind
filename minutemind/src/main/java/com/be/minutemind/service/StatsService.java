package com.be.minutemind.service;

import com.be.minutemind.dtos.response.HeatmapResponse;
import com.be.minutemind.dtos.response.StatsSummaryResponse;

import java.util.List;

public interface StatsService {
    StatsSummaryResponse getSummary(Long userId, String timezone);
    List<HeatmapResponse> getHeatmap(Long userId, int days);
}
