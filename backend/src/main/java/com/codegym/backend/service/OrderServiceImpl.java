package com.codegym.backend.service;

import com.codegym.backend.dto.CartItemResponse;
import com.codegym.backend.dto.CartResponseDTO;
import com.codegym.backend.dto.InvoiceDetailResponseDTO;
import com.codegym.backend.entity.*;
import com.codegym.backend.enums.*;
import com.codegym.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OrderServiceImpl implements OrderService {

    private final TablesRepository tablesRepository;
    private final TableOrderRepository tableOrderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ItemRepository itemRepository;
    private final FeedbackRepository feedbackRepository;

    // ==========================================
    // I. QUẢN LÝ TRẠNG THÁI BÀN & GIỎ HÀNG
    // ==========================================

    @Override
    @Transactional
    public void updateTableServiceStatus(Long tableId, ServiceStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái phục vụ không được để trống!");
        }

        Tables table = getTableEntity(tableId);
        table.setServiceStatus(status);

        // Cập nhật trạng thái occupies đồng bộ
        table.setIsOccupied(status != ServiceStatus.EMPTY);

        tablesRepository.save(table);
    }

    @Override
    @Transactional
    public void addItemToCart(Long tableId, Long itemId, Integer quantity, String note) {
        Tables table = getTableEntity(tableId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại với ID: " + itemId));

        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseGet(() -> {
                    table.setIsOccupied(true);
                    if (table.getServiceStatus() == null || table.getServiceStatus() == ServiceStatus.EMPTY) {
                        table.setServiceStatus(ServiceStatus.SERVING);
                    }
                    tablesRepository.save(table);

                    TableOrder newOrder = TableOrder.builder()
                            .table(table)
                            .openAt(LocalDateTime.now())
                            .totalAmount(BigDecimal.ZERO)
                            .status(StatusTableOrder.OPEN)
                            .build();
                    return tableOrderRepository.save(newOrder);
                });

        List<OrderDetail> existingDetails = orderDetailRepository
                .findByOrderTableOrderIdAndItemItemIdAndStatus(order.getTableOrderId(), itemId, StatusOrderDetail.PENDING);

        if (!existingDetails.isEmpty()) {
            OrderDetail detail = existingDetails.get(0);
            int currentQty = detail.getQuantity() != null ? detail.getQuantity() : 0;
            detail.setQuantity(currentQty + (quantity != null ? quantity : 1));
            if (note != null && !note.trim().isEmpty()) {
                detail.setNote(note);
            }
        } else {
            BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO;
            OrderDetail newDetail = OrderDetail.builder()
                    .order(order)
                    .item(item)
                    .quantity(quantity != null ? quantity : 1)
                    .unitPrice(price)
                    .note(note)
                    .status(StatusOrderDetail.PENDING)
                    .build();

            orderDetailRepository.save(newDetail);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCartOverview(Long tableId) {
        Tables table = getTableEntity(tableId);

        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElse(null);

        if (order == null) {
            return CartResponseDTO.builder()
                    .tableId(table.getTableId())
                    .tableName(table.getTableName())
                    .currentTotalAmount(BigDecimal.ZERO)
                    .orderedItems(List.of())
                    .pendingItems(List.of())
                    .build();
        }

        List<OrderDetail> allDetails = orderDetailRepository.findByOrderTableOrderId(order.getTableOrderId());
        List<CartItemResponse> pendingItems = new ArrayList<>();
        List<CartItemResponse> orderedItems = new ArrayList<>();

        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (OrderDetail detail : allDetails) {
            BigDecimal price = getSafeUnitPrice(detail);
            int qty = detail.getQuantity() != null ? detail.getQuantity() : 0;
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(qty));

            if (detail.getStatus() == StatusOrderDetail.PENDING) {
                pendingItems.add(mapToCartItemResponse(detail));
                calculatedTotal = calculatedTotal.add(itemTotal);
            } else if (detail.getStatus() != StatusOrderDetail.CANCELLED) {
                orderedItems.add(mapToCartItemResponse(detail));
                calculatedTotal = calculatedTotal.add(itemTotal);
            }
        }

        return CartResponseDTO.builder()
                .tableOrderId(order.getTableOrderId())
                .tableId(table.getTableId())
                .tableName(table.getTableName())
                .currentTotalAmount(calculatedTotal)
                .orderedItems(orderedItems)
                .pendingItems(pendingItems)
                .build();
    }

    @Override
    @Transactional
    public void updateCartItemDetail(Long tableId, Long itemId, Integer newQuantity, String newNote) {
        Tables table = getTableEntity(tableId);
        TableOrder order = getOpenOrder(table.getTableId());

        List<OrderDetail> existingDetails = orderDetailRepository
                .findByOrderTableOrderIdAndItemItemIdAndStatus(order.getTableOrderId(), itemId, StatusOrderDetail.PENDING);

        if (existingDetails.isEmpty()) {
            throw new RuntimeException("Món ăn không tồn tại trong giỏ tạm!");
        }

        OrderDetail detail = existingDetails.get(0);
        if (newQuantity != null && newQuantity > 0) detail.setQuantity(newQuantity);
        if (newNote != null) detail.setNote(newNote);
        
        orderDetailRepository.save(detail);
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long tableId, Long itemId) {
        Tables table = getTableEntity(tableId);
        TableOrder order = getOpenOrder(table.getTableId());

        List<OrderDetail> existingDetails = orderDetailRepository
                .findByOrderTableOrderIdAndItemItemIdAndStatus(order.getTableOrderId(), itemId, StatusOrderDetail.PENDING);

        if (!existingDetails.isEmpty()) {
            orderDetailRepository.delete(existingDetails.get(0));
        }
    }

    @Override
    @Transactional
    public void clearTemporaryCart(Long tableId) {
        Tables table = getTableEntity(tableId);
        TableOrder order = getOpenOrder(table.getTableId());

        List<OrderDetail> pendingDetails = orderDetailRepository
                .findByOrderTableOrderIdAndStatus(order.getTableOrderId(), StatusOrderDetail.PENDING);

        if (!pendingDetails.isEmpty()) {
            orderDetailRepository.deleteAllInBatch(pendingDetails);
        }
    }

    @Override
    @Transactional
    public void confirmOrder(Long tableId) {
        Tables table = getTableEntity(tableId);
        TableOrder order = getOpenOrder(table.getTableId());

        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(order.getTableOrderId());
        boolean hasPending = details.stream().anyMatch(d -> d.getStatus() == StatusOrderDetail.PENDING);

        if (!hasPending) {
            throw new RuntimeException("Không có món mới nào trong giỏ hàng tạm!");
        }

        processPendingToOrderedAndRecalculateTotal(order, details);
        
        table.setServiceStatus(ServiceStatus.WAITING_FOOD);
        tablesRepository.save(table);
    }

    // ==========================================
    // II. HÓA ĐƠN & CHI TIẾT
    // ==========================================

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

        Date paidAt = order.getCloseAt() != null 
                ? Date.from(order.getCloseAt().atZone(ZoneId.systemDefault()).toInstant()) 
                : null;

        return InvoiceDetailResponseDTO.builder()
                .orderId(order.getTableOrderId())
                .invoiceCode(String.format("#HD%04d", order.getTableOrderId()))
                .tableId(order.getTable() != null ? order.getTable().getTableId() : null)
                .tableName(order.getTable() != null ? order.getTable().getTableName() : "Mang về")
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0)
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .paidAt(paidAt)
                .items(itemDTOs)
                .build();
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

    private Tables getTableEntity(Long tableId) {
        return tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));
    }

    private TableOrder getOpenOrder(Long tableId) {
        return tableOrderRepository.findByTableTableIdAndStatus(tableId, StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn đang mở cho bàn ID: " + tableId));
    }

    private BigDecimal getSafeUnitPrice(OrderDetail detail) {
        if (detail.getUnitPrice() != null) return detail.getUnitPrice();
        if (detail.getItem() != null && detail.getItem().getPrice() != null) return detail.getItem().getPrice();
        return BigDecimal.ZERO;
    }

    private void processPendingToOrderedAndRecalculateTotal(TableOrder order, List<OrderDetail> details) {
        BigDecimal totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

        for (OrderDetail detail : details) {
            if (detail.getStatus() == StatusOrderDetail.PENDING) {
                detail.setStatus(StatusOrderDetail.ORDERED);
                Item item = detail.getItem();
                if (item != null) {
                    int currentCount = item.getTotalOrderCount() != null ? item.getTotalOrderCount() : 0;
                    int qty = detail.getQuantity() != null ? detail.getQuantity() : 0;
                    item.setTotalOrderCount(currentCount + qty);
                }
                BigDecimal price = getSafeUnitPrice(detail);
                int qty = detail.getQuantity() != null ? detail.getQuantity() : 0;
                totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(qty)));
            }
        }
        order.setTotalAmount(totalAmount);
        tableOrderRepository.save(order);
    }

    private CartItemResponse mapToCartItemResponse(OrderDetail detail) {
        return CartItemResponse.builder()
                .orderDetailId(detail.getOrderDetailId())
                .itemId(detail.getItem() != null ? detail.getItem().getItemId() : null)
                .itemName(detail.getItem() != null ? detail.getItem().getItemName() : null)
                .price(getSafeUnitPrice(detail))
                .quantity(detail.getQuantity() != null ? detail.getQuantity() : 0)
                .note(detail.getNote())
                .status(detail.getStatus() != null ? detail.getStatus().name() : null)
                .tableName(detail.getOrder() != null && detail.getOrder().getTable() != null 
                        ? detail.getOrder().getTable().getTableName() : null)
                .build();
    }
}