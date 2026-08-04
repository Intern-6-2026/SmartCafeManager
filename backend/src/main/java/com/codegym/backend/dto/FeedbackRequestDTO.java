package com.codegym.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequestDTO {

    @NotBlank(message = "Nội dung đánh giá không được để trống")
    @Size(max = 255, message = "Nội dung đánh giá tối đa 255 ký tự") // 👈 Khống chế độ dài ký tự
    private String content;

    @NotNull(message = "Vui lòng chọn số sao đánh giá")
    @Min(value = 1, message = "Số sao đánh giá tối thiểu là 1")      // 👈 Khống chế số sao từ 1 đến 5
    @Max(value = 5, message = "Số sao đánh giá tối đa là 5")
    private Integer rating;

    private String senderName;
    
    private String email;
    
    private String imageUrl; // URL ảnh nhận từ Cloudinary/Firebase/Server

    @NotNull(message = "Bạn cần đăng nhập để gửi đánh giá!")          // 👈 Bắt buộc có customerId (luồng mới)
    private Long customerId;

    @NotNull(message = "Vui lòng chọn món ăn cần đánh giá!")
    private Long itemId;     // Đánh giá cho món ăn nào
}