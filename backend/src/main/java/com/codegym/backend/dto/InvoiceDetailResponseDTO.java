package com.codegym.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDetailResponseDTO {

    private Long orderId;
    private String invoiceCode;      // VD: #HD0001
    private String tableName;        // VD: Bàn 01
    private Double totalAmount;      // Tổng tiền
    private Date paidAt;             // Thời gian thanh toán
    private List<OrderItemDTO> items; // Danh sách món đã gọi

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private Long itemId;
        private String itemName;
        private String itemImage;
        private Double price;
        private Integer quantity;
        private Boolean hasFeedback; // 👈 true: Đã đánh giá rồi | false: Chưa đánh giá (FE hiện nút click)
    }
}