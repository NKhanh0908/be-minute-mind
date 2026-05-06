package com.be.minutemind.service;

import com.be.minutemind.dtos.request.SortOrderRequest;
import com.be.minutemind.dtos.request.TaskRequest;
import com.be.minutemind.dtos.response.TaskResponse;
import com.be.minutemind.enums.TaskStatus;

import java.util.List;

public interface TaskService {
    List<TaskResponse> getTasksByGoal(Long userId, Long goalId);
    TaskResponse createTask(Long userId, TaskRequest request);
    TaskResponse updateTask(Long userId, Long taskId, TaskRequest request);
    TaskResponse updateTaskStatus(Long userId, Long taskId, TaskStatus status);
    void deleteTask(Long userId, Long taskId);
    void updateSortOrder(Long userId, Long goalId, SortOrderRequest request);
}
