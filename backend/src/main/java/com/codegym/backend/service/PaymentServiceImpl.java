package com.codegym.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codegym.backend.dto.TableOrderInvoiceDTO;
import com.codegym.backend.dto.TableOrderSummaryDTO;
import com.codegym.backend.dto.TableOrderSummaryDTO.OrderDetailDTO;
import com.codegym.backend.entity.OrderDetail;
import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.enums.PaymentMethod;
import com.codegym.backend.enums.StatusTableOrder;
import com.codegym.backend.repository.OrderDetailRepository;
import com.codegym.backend.repository.TableOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

        private final TableOrderRepository tableOrderRepository;
        private final OrderDetailRepository orderDetailRepository;

        @Override
        @Transactional
        public void processCashPayment(Long tableId) {
                requestCheckout(tableId, PaymentMethod.CASH);
        }

        @Override
        @Transactional
        public void requestCheckout(Long tableId, PaymentMethod paymentMethod) {
                TableOrder order = tableOrderRepository
                                .findByTableTableIdAndStatus(tableId, StatusTableOrder.OPEN)
                                .orElseThrow(() -> new RuntimeException("Bàn " + tableId + " không có hóa đơn mở!"));

                // Khách yêu cầu: Đổi sang WAITING_PAYMENT, giữ nguyên paidAt/closeAt = null
                order.setStatus(StatusTableOrder.WAITING_PAYMENT);
                order.setPaymentMethod(paymentMethod);
                tableOrderRepository.save(order);
        }

        @Override
        @Transactional
        public void completeCheckout(Long tableId, PaymentMethod paymentMethod) {
                // Nhân viên duyệt: Chấp nhận đơn OPEN hoặc WAITING_PAYMENT
                TableOrder order = tableOrderRepository
                                .findByTableTableIdAndStatusIn(tableId,
                                                List.of(StatusTableOrder.OPEN, StatusTableOrder.WAITING_PAYMENT))
                                .orElseThrow(() -> new RuntimeException(
                                                "Bàn " + tableId + " không có hóa đơn chờ hoàn tất thanh toán!"));

                order.setStatus(StatusTableOrder.PAID);
                order.setPaymentMethod(paymentMethod != null ? paymentMethod : PaymentMethod.CASH);
                order.setPaidAt(LocalDateTime.now());
                order.setCloseAt(LocalDateTime.now());

                tableOrderRepository.save(order);
        }

        @Override
        @Transactional(readOnly = true)
        public TableOrderSummaryDTO getInvoiceSummaryDTO(Long tableId) {
                TableOrder order = tableOrderRepository
                                .findByTableTableIdAndStatusIn(tableId,
                                                List.of(StatusTableOrder.OPEN, StatusTableOrder.WAITING_PAYMENT))
                                .orElseThrow(() -> new RuntimeException(
                                                "Bàn " + tableId + " không có hóa đơn mở hoặc chờ thanh toán!"));

                BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
                List<OrderDetail> details = orderDetailRepository.findByOrder(order);

                return TableOrderSummaryDTO.builder()
                                .tableOrderId(order.getTableOrderId())
                                .tableId(tableId)
                                .tableName(order.getTable() != null ? order.getTable().getTableName()
                                                : "Bàn " + tableId)
                                .totalAmount(total)
                                .orderStatus(order.getStatus() != null ? order.getStatus().name() : null)
                                .openAt(order.getOpenAt())
                                .orderDetails(mapToOrderDetailDTOList(details))
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public TableOrderInvoiceDTO getCurrentInvoice(Long tableId, Long tableOrderId) {
                TableOrder order;

                if (tableOrderId != null) {
                        order = tableOrderRepository.findById(tableOrderId)
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Không tìm thấy hóa đơn mã: " + tableOrderId));
                } else {
                        order = tableOrderRepository
                                        .findByTableTableIdAndStatusIn(tableId,
                                                        List.of(StatusTableOrder.OPEN,
                                                                        StatusTableOrder.WAITING_PAYMENT))
                                        .orElseThrow(() -> new RuntimeException("Bàn " + tableId
                                                        + " không có hóa đơn mở hoặc chờ thanh toán!"));
                }

                List<OrderDetail> details = orderDetailRepository.findByOrder(order);
                List<OrderDetailDTO> itemDTOs = mapToOrderDetailDTOList(details);
                BigDecimal finalTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

                return TableOrderInvoiceDTO.builder()
                                .tableOrderId(order.getTableOrderId())
                                .tableName(order.getTable() != null ? order.getTable().getTableName() : null)
                                .totalAmount(finalTotal)
                                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name()
                                                : null)
                                .status(order.getStatus() != null ? order.getStatus().name() : null)
                                .createdAt(order.getOpenAt())
                                .paidAt(order.getPaidAt())
                                .items(itemDTOs)
                                .build();
        }

        private List<OrderDetailDTO> mapToOrderDetailDTOList(List<OrderDetail> details) {
                if (details == null || details.isEmpty()) {
                        return Collections.emptyList();
                }

                return details.stream()
                                .map(detail -> {
                                        BigDecimal priceBD = detail.getUnitPrice() != null ? detail.getUnitPrice()
                                                        : BigDecimal.ZERO;

                                        return OrderDetailDTO.builder()
                                                        .orderDetailId(detail.getOrderDetailId())
                                                        .itemName(detail.getItem() != null
                                                                        ? detail.getItem().getItemName()
                                                                        : "Món ăn")
                                                        .unitPrice(priceBD.longValue())
                                                        .quantity(detail.getQuantity() != null ? detail.getQuantity()
                                                                        : 0)
                                                        .note(detail.getNote())
                                                        .status(detail.getStatus() != null ? detail.getStatus().name()
                                                                        : null)
                                                        .itemId(detail.getItem() != null ? detail.getItem().getItemId()
                                                                        : null)
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }
}