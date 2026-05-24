package com.be.minutemind.controller;

import com.be.minutemind.annotation.CurrentUser;
import com.be.minutemind.dtos.request.UpdateProfileRequest;
import com.be.minutemind.dtos.response.UserResponse;
import com.be.minutemind.exception.ApiResponse;
import com.be.minutemind.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile APIs")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Lấy thông tin profile hiện tại")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@CurrentUser Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(userId)));
    }

    @Operation(summary = "Cập nhật thông tin profile (name, timezone, streakThresholdMinutes)")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @CurrentUser Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(userId, request)));
    }

    @Operation(summary = "Upload / thay đổi avatar (multipart/form-data, field: file)")
    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateAvatar(
            @CurrentUser Long userId,
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateAvatar(userId, file)));
    }

    @Operation(summary = "Xóa avatar (đặt về null)")
    @DeleteMapping("/me/avatar")
    public ResponseEntity<ApiResponse<Void>> removeAvatar(@CurrentUser Long userId) {
        userService.removeAvatar(userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
