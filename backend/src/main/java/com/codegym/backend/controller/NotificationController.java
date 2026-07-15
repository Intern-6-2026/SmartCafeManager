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
     * Đăng ký (Subscribe) nhận thông báo theo thời gian thực (real-time) cho màn
     * hình Nhân viên hoặc Quản lý. API này sử dụng công nghệ Server-Sent Events
     * (SSE) với định dạng TEXT_EVENT_STREAM_VALUE. Client (giao diện của nhân
     * viên) sẽ gọi API này để mở và duy trì một kết nối liên tục một chiều từ
     * Server tới Client, giúp Server có thể chủ động đẩy (push) các thông báo
     * mới nhất xuống màn hình ngay lập tức mà không cần Client phải gửi yêu cầu
     * liên tục (polling).
     *
     * Đường dẫn API: GET http://localhost:8080/api/v1/auth/notification/subscribe
     */
    @GetMapping(value = "/api/v1/auth/notification/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return notificationService.addEmitter();
    }

    /**
     * Gửi thông báo từ bàn ăn khi khách hàng có yêu cầu (ví dụ: "Gọi món" hoặc
     * "Gọi phục vụ"). Khi khách hàng nhấn nút trên thiết bị tại bàn, thiết bị sẽ
     * gọi API này, truyền lên tên bàn (tableName) và loại yêu cầu (actionType).
     * Server sẽ tạo một thông điệp (message) và phát (broadcast) thông điệp đó
     * tới tất cả các màn hình (emitters) của nhân viên đang lắng nghe ở luồng
     * API subscribe phía trên.
     *
     * Đường dẫn API: POST http://localhost:8080/api/v1/auth/notification/send
     *
     * Tham số:
     * - tableName: Tên/số hiệu bàn gửi yêu cầu.
     * - actionType: Loại yêu cầu (VD: "Gọi món", "Gọi phục vụ").
     */
    @PostMapping("/api/v1/auth/notification/send")
    public ResponseEntity<String> sendNotification(
            @RequestParam String tableName,
            @RequestParam String actionType) {

        String message = "Bàn " + tableName + " vừa yêu cầu: [" + actionType + "]";
        notificationService.sendNotification(message);

        return ResponseEntity.ok("Đã gửi thông báo thành công!");
    }
}