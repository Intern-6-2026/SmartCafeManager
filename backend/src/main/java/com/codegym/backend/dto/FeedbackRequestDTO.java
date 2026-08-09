package com.codegym.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email; 

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequestDTO {

    @Size(max = 255, message = "Nội dung đánh giá tối đa 255 ký tự")
    private String content;

    @NotNull(message = "Vui lòng chọn số sao đánh giá")
    @Min(value = 1, message = "Số sao đánh giá tối thiểu là 1")
    @Max(value = 5, message = "Số sao đánh giá tối đa là 5")
    private Integer rating;
    @NotNull(message = "Vui lòng cung cấp ID đơn hàng")
    private Long orderId; // bắy buộc phải có  để kiểm tra đã mua món hay chưa
    // Không dùng @NotBlank ở DTO: Nếu chưa đăng nhập thì Service sẽ kiểm tra bắt buộc nhập
    private String senderName;
    
    @Email(message = "Email không hợp lệ!")
    private String email;
    
    private String imageUrl; 

    @NotNull(message = "Vui lòng chọn món ăn cần đánh giá!")
    private Long itemId;
    // - Đã đăng nhập: Backend tự lấy ID qua Token gửi kèm.
    // - Khách vãng lai: Trường này bằng null trong Database.
}