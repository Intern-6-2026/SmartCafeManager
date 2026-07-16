package com.codegym.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.codegym.backend.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Subscribe to real-time notifications for staff or manager screens.
     * API Link: http://localhost:8080/api/v1/auth/notification/subscribe
     */
    @GetMapping(value = "/api/v1/auth/notification/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return notificationService.addEmitter();
    }

    /**
     * Send a notification from a table when a customer requests food or service.
     * API Link: http://localhost:8080/api/v1/auth/notification/send
     */
    @PostMapping("/api/v1/auth/notification/send")
    public ResponseEntity<String> sendNotification(
            @RequestParam String tableName,
            @RequestParam String actionType) {

        String message = "Table " + tableName + " has just requested: [" + actionType + "]";
        notificationService.sendNotification(message);

        return ResponseEntity.ok("Notification sent successfully!");
    }
}