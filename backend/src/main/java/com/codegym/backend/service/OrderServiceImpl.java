package com.codegym.backend.service;

import com.codegym.backend.dto.InvoiceDetailResponseDTO;
import com.codegym.backend.entity.*;
import com.codegym.backend.enums.*;
import com.codegym.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OrderServiceImpl implements OrderService {

    private final TablesRepository tablesRepository;
    private final TableOrderRepository tableOrderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ItemRepository itemRepository;
    private final FeedbackRepository feedbackRepository;

    @Override
    @Transactional
    public void updateTableServiceStatus(Long tableId, ServiceStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái phục vụ không được để trống!");
        }

        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));
        
        table.setServiceStatus(status);
        table.setIsOccupied(status != ServiceStatus.EMPTY);
        tablesRepository.save(table);
    }

    @Override
@Transactional
public void cancelOrderItemByCustomer(Long orderDetailId, String reason) {
    OrderDetail detail = orderDetailRepository.findById(orderDetailId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết món ăn ID: " + orderDetailId));

    if (detail.getStatus() != StatusOrderDetail.ORDERED) {
        throw new RuntimeException("Không thể hủy món do Bếp đã nhận chế biến hoặc đã phục vụ!");
    }

    // LẤY SỐ LƯỢNG AN TOÀN: Nếu trong DB bị âm hoặc null, ép về số dương chuẩn
    int rawQty = detail.getQuantity() != null ? detail.getQuantity() : 0;
    if (rawQty <= 0) {
        log.warn("Cảnh báo: OrderDetail ID {} có số lượng không hợp lệ ({}), tự động điều chỉnh về 1", orderDetailId, rawQty);
        rawQty = 1;
    }
    final int qty = rawQty; // Dùng số lượng dương chuẩn để tính toán

    detail.setStatus(StatusOrderDetail.CANCELLED);
    if (reason != null && !reason.trim().isEmpty()) {
        String currentNote = detail.getNote() != null ? detail.getNote() + " | " : "";
        detail.setNote(currentNote + "Khách hủy: " + reason);
    }
    orderDetailRepository.save(detail);

    // Cập nhật lại số lượng đã bán của Item
    Item item = detail.getItem();
    if (item != null) {
        int currentCount = item.getTotalOrderCount() != null ? item.getTotalOrderCount() : 0;
        item.setTotalOrderCount(Math.max(0, currentCount - qty));
        itemRepository.save(item);
    }

    // Cập nhật lại tổng tiền hóa đơn
    TableOrder order = detail.getOrder();
    if (order != null) {
        BigDecimal price = getSafeUnitPrice(detail);
        BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(qty)); // Luôn ra số dương

        BigDecimal currentTotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        order.setTotalAmount(currentTotal.subtract(itemTotal).max(BigDecimal.ZERO));
        tableOrderRepository.save(order);
    }
}

    @Override
    @Transactional(readOnly = true)
    public InvoiceDetailResponseDTO getInvoiceDetailForCustomer(Long orderId, Long customerId) {
        TableOrder order = tableOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn ID: " + orderId));

        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderTableOrderId(orderId);

        List<InvoiceDetailResponseDTO.OrderItemDTO> itemDTOs = orderDetails.stream()
                .map(detail -> {
                    Item item = detail.getItem();
                    Long itemId = (item != null) ? item.getItemId() : null;
                    String itemName = (item != null) ? item.getItemName() : "Món đã ngưng bán";
                    String itemImage = (item != null) ? item.getImageUrl() : null;

                    Double unitPrice = (detail.getUnitPrice() != null) ? detail.getUnitPrice().doubleValue() : 0.0;
                    int quantity = (detail.getQuantity() != null) ? detail.getQuantity() : 0;

                    boolean hasFeedback = false;
                    if (customerId != null && itemId != null) {
                        hasFeedback = feedbackRepository
                                .existsByCustomerCustomerIdAndItemItemIdAndDeletedAtIsNull(customerId, itemId);
                    }

                    return InvoiceDetailResponseDTO.OrderItemDTO.builder()
                            .itemId(itemId)
                            .itemName(itemName)
                            .itemImage(itemImage)
                            .price(unitPrice)
                            .quantity(quantity)
                            .totalPrice(unitPrice * quantity)
                            .hasFeedback(hasFeedback)
                            .build();
                })
                .collect(Collectors.toList());

        LocalDateTime paidDateTime = order.getPaidAt() != null ? order.getPaidAt() : order.getCloseAt();

        return InvoiceDetailResponseDTO.builder()
                .orderId(order.getTableOrderId())
                .invoiceCode(String.format("#HD%04d", order.getTableOrderId()))
                .tableId(order.getTable() != null ? order.getTable().getTableId() : null)
                .tableName(order.getTable() != null ? order.getTable().getTableName() : "Mang về")
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0)
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .paidAt(toDate(paidDateTime))
                .items(itemDTOs)
                .build();
    }

    // --- PRIVATE HELPER METHODS ---

    private BigDecimal getSafeUnitPrice(OrderDetail detail) {
        if (detail.getUnitPrice() != null) return detail.getUnitPrice();
        if (detail.getItem() != null && detail.getItem().getPrice() != null) return detail.getItem().getPrice();
        return BigDecimal.ZERO;
    }

    private Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}