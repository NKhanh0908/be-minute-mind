package com.be.minutemind.controller;

import com.be.minutemind.annotation.CurrentUser;
import com.be.minutemind.dtos.request.SessionCompleteRequest;
import com.be.minutemind.dtos.request.SessionStartRequest;
import com.be.minutemind.dtos.response.ActiveSessionResponse;
import com.be.minutemind.exception.ApiResponse;
import com.be.minutemind.service.WorkSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Focus Session APIs")
public class SessionController {

    private final WorkSessionService workSessionService;

    @Operation(summary = "Get current session", description = "Retrieve the active focus session for the user", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active session retrieved", content = @Content(schema = @Schema(implementation = ActiveSessionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No active session found")
    })
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<ActiveSessionResponse>> getCurrentSession(@CurrentUser Long userId) {
        return ResponseEntity.ok(ApiResponse.success(workSessionService.getCurrentSession(userId)));
    }

    @Operation(summary = "Start session", description = "Start a new focus session", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Session started", content = @Content(schema = @Schema(implementation = ActiveSessionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Already have an active session")
    })
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<ActiveSessionResponse>> startSession(@CurrentUser Long userId, @Valid @RequestBody SessionStartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(workSessionService.startSession(userId, request)));
    }

    @Operation(summary = "Heartbeat", description = "Send a heartbeat to keep the session alive", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Heartbeat processed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<Void> heartbeat(@CurrentUser Long userId, @PathVariable Long id, @RequestParam int currentActualMinutes) {
        workSessionService.heartbeat(userId, id, currentActualMinutes);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Complete session", description = "Complete the focus session", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Session completed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeSession(@CurrentUser Long userId, @PathVariable Long id, @Valid @RequestBody SessionCompleteRequest request) {
        workSessionService.completeSession(userId, id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Discard session", description = "Discard the active session without saving progress", responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Session discarded"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Session not found")
    })
    @PostMapping("/{id}/discard")
    public ResponseEntity<Void> discardSession(@CurrentUser Long userId, @PathVariable Long id) {
        workSessionService.discardSession(userId, id);
        return ResponseEntity.noContent().build();
    }
}
