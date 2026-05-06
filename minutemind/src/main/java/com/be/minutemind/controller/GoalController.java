package com.be.minutemind.controller;

import com.be.minutemind.annotation.CurrentUser;
import com.be.minutemind.dtos.request.GoalRequest;
import com.be.minutemind.dtos.request.SortOrderRequest;
import com.be.minutemind.dtos.response.GoalResponse;
import com.be.minutemind.exception.ApiResponse;
import com.be.minutemind.service.GoalService;
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
@RequestMapping("/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Goal Management APIs")
public class GoalController {

    private final GoalService goalService;

    @Operation(summary = "Get user goals", description = "Retrieve all goals for the current user", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved goals")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoals(@CurrentUser Long userId) {
        return ResponseEntity.ok(ApiResponse.success(goalService.getUserGoals(userId)));
    }

    @Operation(summary = "Create goal", description = "Create a new goal for the user", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Goal created successfully", content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(@CurrentUser Long userId, @Valid @RequestBody GoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(goalService.createGoal(userId, request)));
    }

    @Operation(summary = "Update goal", description = "Update an existing goal", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Goal updated successfully", content = @Content(schema = @Schema(implementation = GoalResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Goal not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(@CurrentUser Long userId, @PathVariable Long id, @Valid @RequestBody GoalRequest request) {
        return ResponseEntity.ok(ApiResponse.success(goalService.updateGoal(userId, id, request)));
    }

    @Operation(summary = "Delete goal", description = "Delete a goal and its associated tasks", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Goal deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Goal not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@CurrentUser Long userId, @PathVariable Long id) {
        goalService.deleteGoal(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update sort order", description = "Update the sorting order of goals", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Sort order updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PatchMapping("/sort")
    public ResponseEntity<Void> updateSortOrder(@CurrentUser Long userId, @Valid @RequestBody SortOrderRequest request) {
        goalService.updateSortOrder(userId, request);
        return ResponseEntity.noContent().build();
    }
}
