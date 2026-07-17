package com.codegym.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.enums.StatusTableOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableOrderSummaryDTO {
    private Long tableOrderId;
    private String tableName;
    private BigDecimal totalAmount;
    private StatusTableOrder orderStatus;
    private ServiceStatus serviceStatus;
    private LocalDateTime openAt;
    
    private List<OrderDetailMinDTO> orderDetails;
}