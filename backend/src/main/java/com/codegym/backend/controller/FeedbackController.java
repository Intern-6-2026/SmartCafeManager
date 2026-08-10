package com.codegym.backend.controller;

import com.codegym.backend.dto.FeedbackRequestDTO;
import com.codegym.backend.dto.FeedbackResponseDTO;
import com.codegym.backend.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // ================= 1. API DÀNH CHO CUSTOMER =================

    /**
     * Khách hàng gửi đánh giá mới (Hỗ trợ cả khách đã đăng nhập & khách vãng lai)
     * URL: POST /api/v1/customer/feedbacks
     */
    @PostMapping("/customer/feedbacks")
    public ResponseEntity<?> createFeedback(
            @Valid @RequestBody FeedbackRequestDTO dto,
            Authentication authentication
    ) {
        String finalEmail;

        // Trường hợp 1: Đã đăng nhập -> Ưu tiên email từ Token/Session nếu Form không gửi
        if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            finalEmail = (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) 
                    ? dto.getEmail().trim() 
                    : authentication.getName();
        } 
        // Trường hợp 2: Khách vãng lai -> Bắt buộc kiểm tra email nhập ở Form
        else {
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Khách vãng lai bắt buộc phải nhập email!"));
            }
            finalEmail = dto.getEmail().trim();
        }

        // Gán email đã xác định vào DTO
        dto.setEmail(finalEmail);

        // Gọi trực tiếp createFeedback để nhận lại FeedbackResponseDTO
        FeedbackResponseDTO createdFeedback = feedbackService.createFeedback(dto);

        return ResponseEntity.ok(createdFeedback);
    }

    /**
     * Khách hàng xem danh sách đánh giá của 1 món ăn cụ thể
     * URL: GET /api/v1/customer/feedbacks/item/{itemId}
     */
    @GetMapping("/customer/feedbacks/item/{itemId}")
    public ResponseEntity<List<FeedbackResponseDTO>> getFeedbacksByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByItem(itemId));
    }


    // ================= 2. API DÀNH CHO STAFF / ADMIN =================

    /**
     * Staff/Admin xem toàn bộ danh sách đánh giá
     * URL: GET /api/v1/staff/feedbacks
     */
    @GetMapping("/staff/feedbacks")
    public ResponseEntity<List<FeedbackResponseDTO>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    /**
     * Staff/Admin xóa hoặc ẩn đánh giá vi phạm
     * URL: DELETE /api/v1/staff/feedbacks/{feedbackId}
     */
    @DeleteMapping("/staff/feedbacks/{feedbackId}")
    public ResponseEntity<Map<String, String>> deleteFeedback(@PathVariable Long feedbackId) {
        feedbackService.deleteFeedback(feedbackId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa đánh giá thành công!"));
    }
}