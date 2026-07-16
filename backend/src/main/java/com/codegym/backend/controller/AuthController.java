package com.codegym.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codegym.backend.dto.ForgotPasswordRequest;
import com.codegym.backend.dto.LoginRequest;
import com.codegym.backend.dto.ResetPasswordRequest;
import com.codegym.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * API đăng nhập người dùng.
     * Đường dẫn API: http://localhost:8080/api/v1/auth/login
     */
    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Yêu cầu email đặt lại mật khẩu.
     * Đường dẫn API: http://localhost:8080/api/v1/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.processForgotPassword(request));
    }

    /**
     * Reset password using the sent recovery token or OTP.
     * API Link: http://localhost:8080/api/v1/auth/reset-password
     */
    @PostMapping("/reset-password")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.processResetPassword(request));
    }
}