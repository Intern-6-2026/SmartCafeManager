package com.codegym.backend.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor; // Bổ sung import này
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponse {
    private Long itemId;
    private String itemCode;
    private Long categoryId;
    private String categoryName;
    private String itemName;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private Boolean isAvailable;
    private Long totalOrderCount;

}