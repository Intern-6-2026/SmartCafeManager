package com.codegym.backend.controller;

import com.codegym.backend.dto.FeedbackRequestDTO;
import com.codegym.backend.dto.FeedbackResponseDTO;
import com.codegym.backend.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/feedbacks")
@CrossOrigin("*")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    /**
     * 1. Khách gửi đánh giá/góp ý mới
     */
    @PostMapping
    public ResponseEntity<FeedbackResponseDTO> createFeedback(@RequestBody FeedbackRequestDTO dto) {
        return ResponseEntity.ok(feedbackService.createFeedback(dto));
    }

    /**
     * 2. Xem danh sách đánh giá của 1 món ăn cụ thể (Dùng cho trang Menu chi tiết món)
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<FeedbackResponseDTO>> getFeedbacksByItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByItem(itemId));
    }

    /**
     * 3. Quản lý xem toàn bộ đánh giá (Dùng cho Admin / Quản lý)
     */
    @GetMapping
    public ResponseEntity<List<FeedbackResponseDTO>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    /**
     * 4. Xóa mềm đánh giá (Dành cho Admin ẩn các feedback vi phạm)
     */
    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Map<String, String>> deleteFeedback(@PathVariable Long feedbackId) {
        feedbackService.deleteFeedback(feedbackId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa đánh giá thành công!"));
    }
}