package com.codegym.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codegym.backend.service.NotificationService;

@RestController
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // 1. API dành cho Header của Nhân viên/Quản lý mở lên để lắng nghe real-time
    // Link: http://localhost:8080/api/v1/auth/notification/subscribe
    @GetMapping(value = "/api/v1/items/**", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return notificationService.addEmitter();
    }

    // 2. API dành cho khách hàng ấn nút [Gọi món] hoặc [Gọi phục vụ]
    // Link: http://localhost:8080/api/v1/auth/notification/send
    @PostMapping("/api/v1/items/**")
    public ResponseEntity<String> sendNotification(
            @RequestParam String tableName,
            @RequestParam String actionType) { 
        
        String message = "Bàn " + tableName + " vừa yêu cầu: [" + actionType + "]";
        notificationService.sendNotification(message);
        return ResponseEntity.ok("Đã gửi thông báo thành công!");
    }
}