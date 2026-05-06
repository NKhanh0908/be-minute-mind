package com.be.minutemind.service;

import com.be.minutemind.dtos.request.GoalRequest;
import com.be.minutemind.dtos.request.SortOrderRequest;
import com.be.minutemind.dtos.response.GoalResponse;

import java.util.List;

public interface GoalService {
    List<GoalResponse> getUserGoals(Long userId);
    GoalResponse createGoal(Long userId, GoalRequest request);
    GoalResponse updateGoal(Long userId, Long goalId, GoalRequest request);
    void deleteGoal(Long userId, Long goalId);
    void updateSortOrder(Long userId, SortOrderRequest request);
}
