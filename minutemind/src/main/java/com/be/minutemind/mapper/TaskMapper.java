package com.be.minutemind.mapper;

import com.be.minutemind.dtos.request.TaskRequest;
import com.be.minutemind.dtos.response.TaskResponse;
import com.be.minutemind.entities.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    TaskResponse toResponse(Task task);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "totalLoggedMinutes", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "sortOrder", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "goal", ignore = true)
    Task toEntity(TaskRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "goalId", ignore = true)
    @Mapping(target = "totalLoggedMinutes", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "sortOrder", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "goal", ignore = true)
    void updateEntityFromRequest(TaskRequest request, @MappingTarget Task task);
}
