package com.codegym.backend.dto;

import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Phải có annotation này
public class ItemResponse {
    private Long itemId;
    private String itemCode;
    
    // Đảm bảo viết CHÍNH XÁC tên thuộc tính này:
    private Long menuCategoryId;
    private String menuCategoryName;
    
    private String itemName;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private Boolean isAvailable;
    private Integer totalOrderCount;
}