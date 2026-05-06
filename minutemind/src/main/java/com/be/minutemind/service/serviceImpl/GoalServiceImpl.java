package com.be.minutemind.service.serviceImpl;

import com.be.minutemind.service.GoalService;
import com.be.minutemind.dtos.request.GoalRequest;
import com.be.minutemind.dtos.request.SortOrderRequest;
import com.be.minutemind.dtos.response.GoalResponse;
import com.be.minutemind.entities.Goal;
import com.be.minutemind.exception.ResourceNotFoundException;
import com.be.minutemind.mapper.GoalMapper;
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
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final TaskRepository taskRepository;
    private final GoalMapper goalMapper;

    @Transactional(readOnly = true)
    public List<GoalResponse> getUserGoals(Long userId) {
        List<Goal> goals = goalRepository.findByUserIdAndDeletedAtIsNullOrderBySortOrderAsc(userId);
        return goals.stream().map(goal -> {
            Integer totalLoggedMinutes = taskRepository.sumLoggedMinutesByGoalId(goal.getId());
            return goalMapper.toResponse(goal, totalLoggedMinutes);
        }).collect(Collectors.toList());
    }

    @Transactional
    public GoalResponse createGoal(Long userId, GoalRequest request) {
        Goal goal = goalMapper.toEntity(request);
        goal.setUserId(userId);
        
        long count = goalRepository.findByUserIdAndDeletedAtIsNullOrderBySortOrderAsc(userId).size();
        goal.setSortOrder((int) count);
        
        goal = goalRepository.save(goal);
        return goalMapper.toResponse(goal, 0);
    }

    @Transactional
    public GoalResponse updateGoal(Long userId, Long goalId, GoalRequest request) {
        Goal goal = getGoalForUser(userId, goalId);
        goalMapper.updateEntityFromRequest(request, goal);
        goal = goalRepository.save(goal);
        
        Integer totalLoggedMinutes = taskRepository.sumLoggedMinutesByGoalId(goal.getId());
        return goalMapper.toResponse(goal, totalLoggedMinutes);
    }

    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        Goal goal = getGoalForUser(userId, goalId);
        goal.setDeletedAt(OffsetDateTime.now());
        goalRepository.save(goal);
    }

    @Transactional
    public void updateSortOrder(Long userId, SortOrderRequest request) {
        List<Goal> goals = goalRepository.findByUserIdAndDeletedAtIsNullOrderBySortOrderAsc(userId);
        Map<Long, Goal> goalMap = goals.stream().collect(Collectors.toMap(Goal::getId, g -> g));
        
        int order = 0;
        for (Long id : request.ids()) {
            Goal goal = goalMap.get(id);
            if (goal != null) {
                goal.setSortOrder(order++);
            }
        }
        
        goalRepository.saveAll(goals);
    }

    private Goal getGoalForUser(Long userId, Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));
                
        if (!goal.getUserId().equals(userId) || goal.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Goal not found");
        }
        
        return goal;
    }
}
