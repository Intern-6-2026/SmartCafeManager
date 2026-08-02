package com.codegym.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableOrderInvoiceDTO {
    private Long tableOrderId;
    private Long tableId;
    private String tableName;
    private BigDecimal totalAmount;
    private String status;
    private String paymentMethod;
    private LocalDateTime openAt;
    private LocalDateTime closeAt;
    private List<OrderItemDTO> orderDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDTO {
        private Long orderDetailId;
        private Long itemId;
        private String itemName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private String note;
        private String status;
        private String imageUrl;
    }
}