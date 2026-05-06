package com.be.minutemind.controller;

import com.be.minutemind.annotation.CurrentUser;
import com.be.minutemind.dtos.request.SortOrderRequest;
import com.be.minutemind.dtos.request.TaskRequest;
import com.be.minutemind.dtos.response.TaskResponse;
import com.be.minutemind.enums.TaskStatus;
import com.be.minutemind.exception.ApiResponse;
import com.be.minutemind.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task Management APIs")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Get tasks by goal", description = "Retrieve all tasks belonging to a specific goal", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tasks retrieved")
    })
    @GetMapping("/goal/{goalId}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByGoal(@CurrentUser Long userId, @PathVariable Long goalId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByGoal(userId, goalId)));
    }

    @Operation(summary = "Create task", description = "Create a new task within a goal", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Task created", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@CurrentUser Long userId, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(taskService.createTask(userId, request)));
    }

    @Operation(summary = "Update task", description = "Update details of an existing task", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task updated", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(@CurrentUser Long userId, @PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskService.updateTask(userId, id, request)));
    }

    @Operation(summary = "Update task status", description = "Change the completion status of a task", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated", content = @Content(schema = @Schema(implementation = TaskResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(@CurrentUser Long userId, @PathVariable Long id, @RequestParam TaskStatus status) {
        return ResponseEntity.ok(ApiResponse.success(taskService.updateTaskStatus(userId, id, status)));
    }

    @Operation(summary = "Delete task", description = "Permanently remove a task", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Task deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@CurrentUser Long userId, @PathVariable Long id) {
        taskService.deleteTask(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update sort order", description = "Change the sorting order of tasks within a goal", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Sort order updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Goal not found")
    })
    @PatchMapping("/goal/{goalId}/sort")
    public ResponseEntity<Void> updateSortOrder(@CurrentUser Long userId, @PathVariable Long goalId, @Valid @RequestBody SortOrderRequest request) {
        taskService.updateSortOrder(userId, goalId, request);
        return ResponseEntity.noContent().build();
    }
}
