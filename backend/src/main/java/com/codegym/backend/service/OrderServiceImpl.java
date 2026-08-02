package com.codegym.backend.service;

import com.codegym.backend.dto.CartItemResponse;
import com.codegym.backend.dto.CartResponseDTO;
import com.codegym.backend.entity.*;
import com.codegym.backend.enums.*;
import com.codegym.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null") // 👉 Tắt toàn bộ cảnh báo Null Safety của VS Code/Eclipse
public class OrderServiceImpl implements OrderService {

    private final TablesRepository tablesRepository;
    private final TableOrderRepository tableOrderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public void addItemToCart(Long tableId, Long itemId, Integer quantity, String note) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại với ID: " + itemId));

        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseGet(() -> {
                    table.setIsOccupied(true);
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
            detail.setQuantity(detail.getQuantity() + quantity);
            if (note != null && !note.trim().isEmpty()) {
                detail.setNote(note);
            }
        } else {
            OrderDetail newDetail = OrderDetail.builder()
                    .order(order)
                    .item(item)
                    .quantity(quantity)
                    .unitPrice(item.getPrice())
                    .note(note)
                    .status(StatusOrderDetail.PENDING)
                    .build();

            orderDetailRepository.save(newDetail);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCartOverview(Long tableId) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));
    
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
    
        BigDecimal calculatedTotal = BigDecimal.ZERO; // 🟢 Tự tính tổng tiền từ tất cả món không bị CANCELLED
    
        for (OrderDetail detail : allDetails) {
            if (detail.getStatus() == StatusOrderDetail.PENDING) {
                pendingItems.add(mapToCartItemResponse(detail));
                // Cộng tiền các món tạm tính
                calculatedTotal = calculatedTotal.add(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
            } else if (detail.getStatus() != StatusOrderDetail.CANCELLED) {
                orderedItems.add(mapToCartItemResponse(detail));
                calculatedTotal = calculatedTotal.add(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
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
        Tables table = tablesRepository.findById(tableId).orElseThrow();
        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn đang mở!"));

        List<OrderDetail> existingDetails = orderDetailRepository
                .findByOrderTableOrderIdAndItemItemIdAndStatus(order.getTableOrderId(), itemId, StatusOrderDetail.PENDING);

        if (existingDetails.isEmpty()) {
            throw new RuntimeException("Món ăn không tồn tại trong giỏ tạm!");
        }

        OrderDetail detail = existingDetails.get(0);
        if (newQuantity != null && newQuantity > 0) detail.setQuantity(newQuantity);
        if (newNote != null) detail.setNote(newNote);
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long tableId, Long itemId) {
        Tables table = tablesRepository.findById(tableId).orElseThrow();
        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn đang mở!"));

        List<OrderDetail> existingDetails = orderDetailRepository
                .findByOrderTableOrderIdAndItemItemIdAndStatus(order.getTableOrderId(), itemId, StatusOrderDetail.PENDING);

        if (!existingDetails.isEmpty()) {
            orderDetailRepository.delete(existingDetails.get(0));
        }
    }

    @Override
    @Transactional
    public void clearTemporaryCart(Long tableId) {
        Tables table = tablesRepository.findById(tableId).orElseThrow();
        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn đang mở!"));

        List<OrderDetail> pendingDetails = orderDetailRepository
                .findByOrderTableOrderIdAndStatus(order.getTableOrderId(), StatusOrderDetail.PENDING);

        if (!pendingDetails.isEmpty()) {
            orderDetailRepository.deleteAllInBatch(pendingDetails);
        }
    }

    @Override
    @Transactional
    public void confirmOrder(Long tableId) {
        Tables table = tablesRepository.findById(tableId).orElseThrow();
        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn đang mở!"));

        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(order.getTableOrderId());
        boolean hasPending = details.stream().anyMatch(d -> d.getStatus() == StatusOrderDetail.PENDING);

        if (!hasPending) {
            throw new RuntimeException("Không có món mới nào trong giỏ hàng tạm!");
        }

        processPendingToOrderedAndRecalculateTotal(order, details);
        table.setServiceStatus(ServiceStatus.WAITING_FOOD);
    }

    private void processPendingToOrderedAndRecalculateTotal(TableOrder order, List<OrderDetail> details) {
        BigDecimal totalAmount = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;

        for (OrderDetail detail : details) {
            if (detail.getStatus() == StatusOrderDetail.PENDING) {
                detail.setStatus(StatusOrderDetail.ORDERED);
                Item item = detail.getItem();
                if (item != null) {
                    int currentCount = item.getTotalOrderCount() != null ? item.getTotalOrderCount() : 0;
                    item.setTotalOrderCount(currentCount + detail.getQuantity());
                }
                BigDecimal itemTotal = detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }
        }
        order.setTotalAmount(totalAmount);
    }

    private CartItemResponse mapToCartItemResponse(OrderDetail detail) {
        return CartItemResponse.builder()
                .orderDetailId(detail.getOrderDetailId())
                .itemId(detail.getItem() != null ? detail.getItem().getItemId() : null)
                .itemName(detail.getItem() != null ? detail.getItem().getItemName() : null)
                .price(detail.getUnitPrice())
                .quantity(detail.getQuantity())
                .note(detail.getNote())
                .status(detail.getStatus() != null ? detail.getStatus().name() : null)
                .tableName(detail.getOrder() != null && detail.getOrder().getTable() != null 
                        ? detail.getOrder().getTable().getTableName() : null)
                .build();
    }
}