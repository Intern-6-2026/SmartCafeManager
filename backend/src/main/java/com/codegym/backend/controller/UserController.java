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
     * Get the current authenticated user's profile.
     * API Link: http://localhost:8080/api/v1/users/profile
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    /**
     * Change the password of the current authenticated user.
     * API Link: http://localhost:8080/api/v1/users/change-password
     */
    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String message = userService.changePassword(request);
        return ResponseEntity.ok(message);
    }

    /**
     * Update the current authenticated user's profile.
     * API Link: http://localhost:8080/api/v1/users/profile
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }
}