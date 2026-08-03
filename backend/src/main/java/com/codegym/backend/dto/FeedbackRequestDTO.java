package com.codegym.backend.dto;

import lombok.Data;

@Data
public class FeedbackRequestDTO {
    private String content;
    private Integer rating;
    private String senderName;
    private String email;
    private String imageUrl;
    private Long customerId; // Có thể null nếu là khách vãng lai
    private Long itemId;     // Đánh giá cho món ăn nào
}