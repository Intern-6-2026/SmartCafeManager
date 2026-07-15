package com.codegym.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codegym.backend.dto.ChangePasswordRequest;
import com.codegym.backend.dto.UpdateProfileRequest;
import com.codegym.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Lấy thông tin hồ sơ (profile) của người dùng hiện đang đăng nhập.
     * Hệ thống sẽ tự động trích xuất định danh người dùng từ JWT Token được gửi kèm
     * trong Header (SecurityContextHolder). Sau đó truy vấn thông tin chi tiết
     * (Nhân viên hoặc Khách hàng).
     * Yêu cầu phân quyền: Người dùng bắt buộc phải đăng nhập hợp lệ
     * (isAuthenticated()).
     * * Đường dẫn API: GET http://localhost:8080/api/v1/users/profile
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    /**
     * Thay đổi mật khẩu của người dùng hiện đang đăng nhập.
     * Yêu cầu người dùng cung cấp mật khẩu cũ (để xác minh chính chủ) và mật khẩu
     * mới.
     * Hệ thống sẽ tự nhận diện người dùng đang thao tác dựa trên Token hiện tại,
     * ngăn chặn việc đổi mật khẩu của tài khoản khác.
     * Yêu cầu phân quyền: Người dùng bắt buộc phải đăng nhập hợp lệ
     * (isAuthenticated()).
     * * Đường dẫn API: PUT http://localhost:8080/api/v1/users/change-password
     */
    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String message = userService.changePassword(request);
        return ResponseEntity.ok(message);
    }

    /**
     * Cập nhật thông tin hồ sơ cá nhân của người dùng hiện đang đăng nhập.
     * Cho phép chỉnh sửa các thông tin như: Họ tên, ngày sinh, giới tính, số điện
     * thoại,
     * địa chỉ, email và hình ảnh đại diện (avatar).
     * Yêu cầu phân quyền: Người dùng bắt buộc phải đăng nhập hợp lệ
     * (isAuthenticated()).
     * * Đường dẫn API: PUT http://localhost:8080/api/v1/users/profile
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }
}