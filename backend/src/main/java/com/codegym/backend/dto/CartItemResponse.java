package com.codegym.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long orderDetailId;
    private Long itemId;
    private String itemName;
    private BigDecimal price;
    private Integer quantity;
    private String tableName;
}