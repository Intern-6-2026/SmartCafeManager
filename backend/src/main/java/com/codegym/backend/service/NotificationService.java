package com.codegym.backend.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class NotificationService {
    // Danh sách lưu tất cả các máy nhân viên/quản lý đang kết nối
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // Hàm để máy nhân viên đăng ký nhận thông báo
    public SseEmitter addEmitter() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // Không giới hạn thời gian timeout
        this.emitters.add(emitter);

        // Xóa emitter khi kết nối bị ngắt hoặc lỗi
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onError((e) -> this.emitters.remove(emitter));

        return emitter;
    }

    // Hàm gửi thông báo đến TẤT CẢ các máy đang kết nối
    public void sendNotification(String message) {
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();
        
        for (SseEmitter emitter : emitters) {
            try {
                // Bắn data xuống Client
                emitter.send(SseEmitter.event().name("notification").data(message));
            } catch (IOException e) {
                deadEmitters.add(emitter); // Lưu lại các kết nối đã chết để xóa
            }
        }
        this.emitters.removeAll(deadEmitters);
    }
}