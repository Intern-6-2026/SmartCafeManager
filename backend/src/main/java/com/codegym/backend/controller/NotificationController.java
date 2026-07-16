package com.codegym.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; // Đổi sang import này
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codegym.backend.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.Data; // Thêm import này để dùng cho class hứng dữ liệu

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * API để nhân viên/quản lý lắng nghe thông báo realtime.
     */
    @GetMapping(value = "/api/v1/auth/notification/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return notificationService.addEmitter();
    }

    /**
     * API khách hàng gửi yêu cầu gọi món hoặc gọi nhân viên.
     * (Đã sửa từ @RequestParam sang @RequestBody để Swagger UI hiển thị ô điền JSON)
     */
    @PostMapping("/api/v1/auth/notification/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationDto request) {

        String message = "Bàn " + request.getTableName() + " vừa yêu cầu: [" + request.getActionType() + "]";
        notificationService.sendNotification(message);

        return ResponseEntity.ok("Đã gửi thông báo thành công!");
    }

    // --- Tự định nghĩa một Class nhỏ ngay tại đây để hứng dữ liệu test trên Swagger ---
    @Data
    public static class NotificationDto {
        private String tableName;
        private String actionType;
    }
}