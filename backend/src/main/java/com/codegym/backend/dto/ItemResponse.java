package com.codegym.backend.dto;

import java.math.BigDecimal;
<<<<<<< HEAD
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
=======
import lombok.*;
>>>>>>> 6f367d9e48f4cbd30c14a9e766cc61f749f977ed

@Data
@NoArgsConstructor
@AllArgsConstructor
<<<<<<< HEAD
@Builder
=======
@Builder // Phải có annotation này
>>>>>>> 6f367d9e48f4cbd30c14a9e766cc61f749f977ed
public class ItemResponse {
    private Long itemId;
    private String itemCode;
    
    // Đã đồng bộ chuẩn với JPQL Constructor của Repository:
    private Long categoryId;
    private String categoryName;
    
    private String itemName;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private Boolean isAvailable;
<<<<<<< HEAD
    private Integer totalOrderCount; // Đã đưa về Integer để khớp Entity và tránh lỗi ép kiểu
=======
    private Integer totalOrderCount;
>>>>>>> 6f367d9e48f4cbd30c14a9e766cc61f749f977ed
}