package com.codegym.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponseDTO {
    private Long orderId;          // ID gốc của đơn
    private String invoiceCode;    // Mã hóa đơn (Ví dụ: #HD001)
    private Long tableId;          // ID bàn
    private String tableName;      // Tên bàn (Ví dụ: Bàn 01)
    private Double totalAmount;    // Tổng tiền
    private Date createdAt;        // Thời gian tạo
    private String status;         // Trạng thái (Đã thanh toán)
}