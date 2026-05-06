package com.be.minutemind.service.serviceImpl;

import com.be.minutemind.service.WorkSessionService;
import com.be.minutemind.service.UpdateStreakService;
import com.be.minutemind.service.CheckBadgeService;
import com.be.minutemind.dtos.request.SessionCompleteRequest;
import com.be.minutemind.dtos.request.SessionStartRequest;
import com.be.minutemind.dtos.response.ActiveSessionResponse;
import com.be.minutemind.entities.Task;
import com.be.minutemind.entities.WorkSession;
import com.be.minutemind.enums.TaskStatus;
import com.be.minutemind.exception.ResourceNotFoundException;
import com.be.minutemind.exception.ValidationException;
import com.be.minutemind.repository.TaskRepository;
import com.be.minutemind.repository.WorkSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkSessionServiceImpl implements WorkSessionService {

    private final WorkSessionRepository workSessionRepository;
    private final TaskRepository taskRepository;
    private final StringRedisTemplate redisTemplate;
    private final UpdateStreakService updateStreakService;
    private final CheckBadgeService checkBadgeService;

    private static final String ACTIVE_SESSION_PREFIX = "vilo:active_session:";

    @Transactional
    public ActiveSessionResponse startSession(Long userId, SessionStartRequest request) {
        Optional<WorkSession> activeSession = workSessionRepository.findByUserIdAndEndedAtIsNull(userId);
        if (activeSession.isPresent()) {
            throw new ValidationException("Session already active with ID: " + activeSession.get().getId());
        }

        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (!task.getUserId().equals(userId) || task.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Task not found");
        }

        OffsetDateTime now = OffsetDateTime.now();
        WorkSession session = WorkSession.builder()
                .userId(userId)
                .taskId(request.taskId())
                .sessionType(request.sessionType())
                .plannedMinutes(request.plannedMinutes())
                .actualMinutes(0)
                .startedAt(now)
                .lastHeartbeatAt(now)
                .completed(false)
                .build();

        session = workSessionRepository.save(session);

        if (task.getStatus() == TaskStatus.TODO) {
            task.setStatus(TaskStatus.IN_PROGRESS);
            taskRepository.save(task);
        }

        redisTemplate.opsForValue().set(ACTIVE_SESSION_PREFIX + userId, session.getId().toString(),
                Duration.ofHours(4));

        return toResponse(session);
    }

    @Transactional
    public void heartbeat(Long userId, Long sessionId, int currentActualMinutes) {
        WorkSession session = getActiveSessionForUser(userId, sessionId);
        session.setActualMinutes(currentActualMinutes);
        session.setLastHeartbeatAt(OffsetDateTime.now());
        workSessionRepository.save(session);

        redisTemplate.opsForValue().set(ACTIVE_SESSION_PREFIX + userId, session.getId().toString(),
                Duration.ofHours(4));
    }

    @Transactional
    public void completeSession(Long userId, Long sessionId, SessionCompleteRequest request) {
        WorkSession session = getActiveSessionForUser(userId, sessionId);

        session.setActualMinutes(request.actualMinutes());
        session.setEndedAt(OffsetDateTime.now());
        session.setNotes(request.notes());

        if (request.completedTask()) {
            Task task = taskRepository.findById(session.getTaskId()).orElse(null);
            if (task != null) {
                task.setStatus(TaskStatus.DONE);
                taskRepository.save(task);
            }
            session.setCompleted(true);
        } else {
            boolean reachedTimer = request.actualMinutes() >= session.getPlannedMinutes() - 1;
            session.setCompleted(reachedTimer);
        }

        workSessionRepository.save(session);

        taskRepository.addLoggedMinutes(session.getTaskId(), request.actualMinutes());

        redisTemplate.delete(ACTIVE_SESSION_PREFIX + userId);

        // Gamification logic
        if (session.isWorkSession() && request.actualMinutes() > 0) {
            updateStreakService.updateStreak(userId, request.actualMinutes());
            checkBadgeService.checkAndAwardBadges(userId);
        }
    }

    @Transactional
    public void discardSession(Long userId, Long sessionId) {
        WorkSession session = getActiveSessionForUser(userId, sessionId);
        session.setEndedAt(OffsetDateTime.now());
        session.setCompleted(false);
        workSessionRepository.save(session);
        redisTemplate.delete(ACTIVE_SESSION_PREFIX + userId);
    }

    @Transactional(readOnly = true)
    public ActiveSessionResponse getCurrentSession(Long userId) {
        return workSessionRepository.findByUserIdAndEndedAtIsNull(userId)
                .map(this::toResponse)
                .orElse(null);
    }

    private WorkSession getActiveSessionForUser(Long userId, Long sessionId) {
        WorkSession session = workSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        if (!session.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Session not found");
        }
        if (session.getEndedAt() != null) {
            throw new ValidationException("Session already ended");
        }
        return session;
    }

    private ActiveSessionResponse toResponse(WorkSession session) {
        return new ActiveSessionResponse(
                session.getId(),
                session.getTaskId(),
                session.getSessionType(),
                session.getPlannedMinutes(),
                session.getStartedAt(),
                session.getLastHeartbeatAt());
    }
}
