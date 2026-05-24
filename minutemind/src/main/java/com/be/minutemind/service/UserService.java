package com.be.minutemind.service;

import com.be.minutemind.dtos.request.UpdateProfileRequest;
import com.be.minutemind.dtos.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse getProfile(Long userId);
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    UserResponse updateAvatar(Long userId, MultipartFile file);
    void removeAvatar(Long userId);
}
