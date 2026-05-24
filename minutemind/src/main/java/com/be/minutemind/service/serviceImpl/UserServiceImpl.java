package com.be.minutemind.service.serviceImpl;

import com.be.minutemind.dtos.request.UpdateProfileRequest;
import com.be.minutemind.dtos.response.UserResponse;
import com.be.minutemind.entities.User;
import com.be.minutemind.exception.ResourceNotFoundException;
import com.be.minutemind.helper.CloudinaryService;
import com.be.minutemind.mapper.UserMapper;
import com.be.minutemind.repository.UserRepository;
import com.be.minutemind.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = findUserById(userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        user.setName(request.name());

        if (request.timezone() != null && !request.timezone().isBlank()) {
            user.setTimezone(request.timezone());
        }
        if (request.streakThresholdMinutes() != null) {
            user.setStreakThresholdMinutes(request.streakThresholdMinutes());
        }

        user = userRepository.save(user);
        log.info("Updated profile for userId={}", userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateAvatar(Long userId, MultipartFile file) {
        User user = findUserById(userId);

        // Xóa avatar cũ trên Cloudinary nếu có
        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            try {
                String publicId = cloudinaryService.extractPublicId(user.getAvatarUrl());
                cloudinaryService.deleteFile(publicId);
                log.info("Deleted old avatar for userId={}, publicId={}", userId, publicId);
            } catch (Exception e) {
                log.warn("Không thể xóa avatar cũ cho userId={}: {}", userId, e.getMessage());
            }
        }

        // Upload avatar mới
        Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, "minute-mind/avatars");
        String newAvatarUrl = (String) uploadResult.get("secure_url");

        user.setAvatarUrl(newAvatarUrl);
        user = userRepository.save(user);
        log.info("Updated avatar for userId={}, url={}", userId, newAvatarUrl);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void removeAvatar(Long userId) {
        User user = findUserById(userId);

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
            try {
                String publicId = cloudinaryService.extractPublicId(user.getAvatarUrl());
                cloudinaryService.deleteFile(publicId);
                log.info("Deleted avatar for userId={}, publicId={}", userId, publicId);
            } catch (Exception e) {
                log.warn("Không thể xóa avatar cho userId={}: {}", userId, e.getMessage());
            }
            user.setAvatarUrl(null);
            userRepository.save(user);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
