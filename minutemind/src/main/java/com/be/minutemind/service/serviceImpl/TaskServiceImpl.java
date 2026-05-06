package com.be.minutemind.service.serviceImpl;

import com.be.minutemind.service.TaskService;
import com.be.minutemind.dtos.request.SortOrderRequest;
import com.be.minutemind.dtos.request.TaskRequest;
import com.be.minutemind.dtos.response.TaskResponse;
import com.be.minutemind.entities.Goal;
import com.be.minutemind.entities.Task;
import com.be.minutemind.enums.TaskStatus;
import com.be.minutemind.exception.ResourceNotFoundException;
import com.be.minutemind.mapper.TaskMapper;
import com.be.minutemind.repository.GoalRepository;
import com.be.minutemind.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final GoalRepository goalRepository;
    private final TaskMapper taskMapper;

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByGoal(Long userId, Long goalId) {
        verifyGoalOwnership(userId, goalId);
        List<Task> tasks = taskRepository.findByGoalIdAndDeletedAtIsNullOrderBySortOrderAsc(goalId);
        return tasks.stream().map(taskMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse createTask(Long userId, TaskRequest request) {
        verifyGoalOwnership(userId, request.goalId());

        Task task = taskMapper.toEntity(request);
        task.setUserId(userId);
        task.setStatus(TaskStatus.TODO);

        long count = taskRepository.findByGoalIdAndDeletedAtIsNullOrderBySortOrderAsc(request.goalId()).size();
        task.setSortOrder((int) count);

        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskRequest request) {
        Task task = getTaskForUser(userId, taskId);

        // Check if goal id changed and verify ownership of new goal
        if (!task.getGoalId().equals(request.goalId())) {
            verifyGoalOwnership(userId, request.goalId());
        }

        taskMapper.updateEntityFromRequest(request, task);
        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long userId, Long taskId, TaskStatus status) {
        Task task = getTaskForUser(userId, taskId);
        task.setStatus(status);
        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        Task task = getTaskForUser(userId, taskId);
        task.setDeletedAt(OffsetDateTime.now());
        taskRepository.save(task);
    }

    @Transactional
    public void updateSortOrder(Long userId, Long goalId, SortOrderRequest request) {
        verifyGoalOwnership(userId, goalId);

        List<Task> tasks = taskRepository.findByGoalIdAndDeletedAtIsNullOrderBySortOrderAsc(goalId);
        Map<Long, Task> taskMap = tasks.stream().collect(Collectors.toMap(Task::getId, t -> t));

        int order = 0;
        for (Long id : request.ids()) {
            Task task = taskMap.get(id);
            if (task != null) {
                task.setSortOrder(order++);
            }
        }

        taskRepository.saveAll(tasks);
    }

    private void verifyGoalOwnership(Long userId, Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
        if (!goal.getUserId().equals(userId) || goal.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Goal not found");
        }
    }

    private Task getTaskForUser(Long userId, Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getUserId().equals(userId) || task.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Task not found");
        }
        return task;
    }
}
