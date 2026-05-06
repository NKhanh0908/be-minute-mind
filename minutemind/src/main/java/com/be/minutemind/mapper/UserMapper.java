package com.be.minutemind.mapper;

import com.be.minutemind.dtos.response.UserResponse;
import com.be.minutemind.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}
