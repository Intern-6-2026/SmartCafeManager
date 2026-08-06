package com.codegym.backend.controller;

import com.codegym.backend.dto.FeedbackRequestDTO;
import com.codegym.backend.dto.FeedbackResponseDTO;
import com.codegym.backend.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1") // 👈 Đưa prefix chung lên Class
@CrossOrigin("*")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // ================= 1. API DÀNH CHO CUSTOMER =================

    /**
     * Khách hàng gửi đánh giá mới
     * URL: POST /api/v1/customer/feedbacks
     */
    @PostMapping("/customer/feedbacks")
    public ResponseEntity<?> createFeedback(@RequestBody FeedbackRequestDTO dto) {
        try {
            FeedbackResponseDTO result = feedbackService.createFeedback(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
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
     * Staff/Admin xem toàn bộ danh sách đánh giá (phục vụ cho giao diện quản lý)
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