package com.codegym.backend.service;

import com.codegym.backend.dto.TableOrderInvoiceDTO;
import com.codegym.backend.dto.TableOrderSummaryDTO;
import com.codegym.backend.entity.*;
import com.codegym.backend.enums.*;
import com.codegym.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class PaymentServiceImpl implements PaymentService {

    private final TablesRepository tablesRepository;
    private final TableOrderRepository tableOrderRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    @Transactional
    public void processCashPayment(Long tableId) {
        Tables table = tablesRepository.findById(tableId).orElseThrow();
        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn đang mở!"));

        order.setPaymentMethod(PaymentMethod.CASH);
        table.setServiceStatus(ServiceStatus.REQUESTING_BILL);
    }

    @Override
    @Transactional
    public void requestCheckout(Long tableId, PaymentMethod paymentMethod) {
        Tables table = tablesRepository.findById(tableId).orElseThrow();
        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        order.setPaymentMethod(paymentMethod);
        table.setServiceStatus(ServiceStatus.REQUESTING_BILL);
    }

    @Override
    @Transactional
    public void completeCheckout(Long tableId, PaymentMethod paymentMethod) {
        Tables table = tablesRepository.findById(tableId).orElseThrow();
        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Bàn không có hóa đơn nào cần thanh toán!"));

        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(order.getTableOrderId());

        BigDecimal totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        for (OrderDetail detail : details) {
            if (detail.getStatus() == StatusOrderDetail.PENDING) {
                detail.setStatus(StatusOrderDetail.ORDERED);
                BigDecimal itemTotal = detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }
        }

        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(StatusTableOrder.PAID);
        order.setCloseAt(LocalDateTime.now());

        table.setIsOccupied(false);
        table.setServiceStatus(ServiceStatus.NORMAL);
    }

    @Override
    @Transactional(readOnly = true)
    public TableOrderSummaryDTO getInvoiceSummaryDTO(Long tableId) {
        Tables table = tablesRepository.findById(tableId).orElseThrow();
        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Bàn không có hóa đơn mở!"));

        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(order.getTableOrderId());

        // 🟢 FIX 1: Chỉ lọc bỏ món CANCELLED, tính tiền cho cả món PENDING/ORDERED/CONFIRMED/SERVED
        BigDecimal calculatedTotal = details.stream()
                .filter(d -> d.getStatus() != StatusOrderDetail.CANCELLED)
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TableOrderSummaryDTO.OrderDetailDTO> detailDTOs = details.stream().map(d -> {
            TableOrderSummaryDTO.OrderDetailDTO dto = new TableOrderSummaryDTO.OrderDetailDTO();
            dto.setOrderDetailId(d.getOrderDetailId());
            dto.setQuantity(d.getQuantity());
            dto.setUnitPrice(d.getUnitPrice() != null ? d.getUnitPrice().longValue() : 0L);
            dto.setNote(d.getNote());
            dto.setStatus(d.getStatus().name());
            if (d.getItem() != null) {
                dto.setItemId(d.getItem().getItemId());
                dto.setItemName(d.getItem().getItemName());
                dto.setImageUrl(d.getItem().getImageUrl());
            }
            return dto;
        }).collect(Collectors.toList());

        TableOrderSummaryDTO summary = new TableOrderSummaryDTO();
        summary.setTableOrderId(order.getTableOrderId());
        summary.setTableName(table.getTableName());
        summary.setTotalAmount(calculatedTotal);
        summary.setOrderStatus(order.getStatus().name());
        summary.setServiceStatus(table.getServiceStatus().name());
        summary.setOpenAt(order.getOpenAt());
        summary.setOrderDetails(detailDTOs);

        return summary;
    }

    @Override
    @Transactional(readOnly = true)
    public TableOrderInvoiceDTO getCurrentInvoice(Long tableId, Long tableOrderId) {
        Tables table = tablesRepository.findById(tableId).orElseThrow();
        TableOrder order = tableOrderRepository
                .findByTableOrderIdAndTableTableIdAndStatus(tableOrderId, tableId, StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn mở!"));

        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(order.getTableOrderId());

        // 🟢 FIX 2: Tự động tính tổng tiền thực tế thay vì lấy order.getTotalAmount() đang bằng 0
        BigDecimal calculatedTotal = details.stream()
                .filter(d -> d.getStatus() != StatusOrderDetail.CANCELLED)
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TableOrderInvoiceDTO.OrderItemDTO> itemDTOs = details.stream().map(d -> {
            BigDecimal totalPrice = d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity()));
            return TableOrderInvoiceDTO.OrderItemDTO.builder()
                    .orderDetailId(d.getOrderDetailId())
                    .itemId(d.getItem() != null ? d.getItem().getItemId() : null)
                    .itemName(d.getItem() != null ? d.getItem().getItemName() : null)
                    .quantity(d.getQuantity())
                    .unitPrice(d.getUnitPrice())
                    .totalPrice(totalPrice)
                    .note(d.getNote())
                    .status(d.getStatus().name())
                    .imageUrl(d.getItem() != null ? d.getItem().getImageUrl() : null)
                    .build();
        }).collect(Collectors.toList());

        return TableOrderInvoiceDTO.builder()
                .tableOrderId(order.getTableOrderId())
                .tableId(table.getTableId())
                .tableName(table.getTableName())
                .totalAmount(calculatedTotal)
                .status(order.getStatus().name())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .openAt(order.getOpenAt())
                .closeAt(order.getCloseAt())
                .orderDetails(itemDTOs)
                .build();
    }
}