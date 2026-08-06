package com.codegym.backend.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codegym.backend.dto.ActiveOrderDTO;
import com.codegym.backend.dto.OrderDetailResponseDTO;
import com.codegym.backend.entity.OrderDetail;
import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.entity.Tables;
import com.codegym.backend.enums.PaymentMethod;
import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.enums.StatusOrderDetail;
import com.codegym.backend.enums.StatusTableOrder;
import com.codegym.backend.exception.AppException;
import com.codegym.backend.repository.OrderDetailRepository;
import com.codegym.backend.repository.TableOrderRepository;
import com.codegym.backend.repository.TablesRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class StaffOrderServiceImpl implements StaffOrderService {

    private final TablesRepository tablesRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final TableOrderRepository tableOrderRepository;
    private final PaymentService paymentService;
    private final SimpMessagingTemplate messagingTemplate;

    // ==========================================
    // I. SƠ ĐỒ BÀN & CHI TIẾT PANELS
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<Tables> getAllTables() {
        return tablesRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Tables getTableInfo(Long tableId) {
        return tablesRepository.findById(tableId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy thông tin bàn ID: " + tableId));
    }

    /**
     * Lấy dữ liệu chi tiết hóa đơn đang active cho Panel bên phải (Giao diện
     * NeoCafé)
     * Trả về ActiveOrderDTO chuẩn hóa
     */
    @Override
    @Transactional(readOnly = true)
    public ActiveOrderDTO getActiveOrderByTable(Long tableId) {
        Tables table = getTableInfo(tableId);

        TableOrder activeOrder = tableOrderRepository
                .findByTableTableIdAndStatus(tableId, StatusTableOrder.OPEN)
                .orElse(null);

        // Trường hợp bàn trống / không có đơn active
        if (activeOrder == null) {
            return ActiveOrderDTO.builder()
                    .tableId(table.getTableId())
                    .tableName(table.getTableName())
                    .serviceStatus(table.getServiceStatus() != null ? table.getServiceStatus().name() : null)
                    .hasActiveOrder(false)
                    .build();
        }

        // Trường hợp bàn đang có đơn
        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(activeOrder.getTableOrderId());

        // Format giờ tạo đơn (Xử lý an toàn cho java.util.Date)
        SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
        String formattedTime = activeOrder.getCreatedAt() != null
                ? timeFormatter.format(activeOrder.getCreatedAt())
                : "";

        // Chuyển đổi danh sách món ăn sang DTO chuẩn
        List<ActiveOrderDTO.OrderItemDto> itemDtos = details.stream()
                .map(detail -> ActiveOrderDTO.OrderItemDto.builder()
                        .orderDetailId(detail.getOrderDetailId())
                        .itemName(detail.getItem().getItemName())
                        .quantity(detail.getQuantity())
                        .unitPrice(detail.getUnitPrice())
                        .subTotal(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())))
                        .status(detail.getStatus() != null ? detail.getStatus().name() : null)
                        .note(detail.getNote())
                        .build())
                .collect(Collectors.toList());

        return ActiveOrderDTO.builder()
                .tableId(table.getTableId())
                .tableName(table.getTableName())
                .serviceStatus(table.getServiceStatus() != null ? table.getServiceStatus().name() : null)
                .hasActiveOrder(true)
                .tableOrderId(activeOrder.getTableOrderId())
                .orderTime(formattedTime)
                .totalAmount(activeOrder.getTotalAmount())
                .items(itemDtos)
                .build();
    }

    @Override
    public List<OrderDetailResponseDTO> getOrderDetailsByTable(Long tableId) {
        TableOrder activeOrder = tableOrderRepository
                .findByTableTableIdAndStatus(tableId, StatusTableOrder.OPEN)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Bàn hiện tại không có đơn hàng active!"));

        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(activeOrder.getTableOrderId());

        // Map từ OrderDetail (Entity) sang OrderDetailResponseDTO
        return details.stream().map(item -> {
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            return OrderDetailResponseDTO.builder()
                    .orderDetailId(item.getOrderDetailId())
                    .itemId(item.getItem() != null ? item.getItem().getItemId() : null)
                    .itemName(item.getItem() != null ? item.getItem().getItemName() : "Món không xác định")
                    .itemImage(item.getItem() != null ? item.getItem().getImageUrl() : null)
                    .quantity(item.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(total)
                    .note(item.getNote())
                    .status(item.getStatus())
                    .build();
        }).collect(Collectors.toList());
    }

    // ==========================================
    // II. THAO TÁC TRÊN ĐƠN HÀNG & THANH TOÁN
    // ==========================================

    /**
     * Duyệt thanh toán tiền mặt (Đổi trạng thái đơn -> PAID & giải phóng bàn về
     * EMPTY)
     */
    @Override
    @Transactional
    public void approveCashPayment(Long tableId) {
        // 1. Tìm đơn hàng đang OPEN của bàn
        TableOrder activeOrder = tableOrderRepository.findByTableTableIdAndStatus(tableId, StatusTableOrder.OPEN)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy đơn hàng đang mở của bàn ID: " + tableId));

        // 2. Chuyển trạng thái đơn hàng sang PAID
        activeOrder.setStatus(StatusTableOrder.PAID);
        tableOrderRepository.save(activeOrder);

        // 3. Gọi thanh toán qua PaymentService
        paymentService.completeCheckout(tableId, PaymentMethod.CASH);

        // 4. Chuyển trạng thái bàn về TRỐNG
        Tables table = getTableInfo(tableId);
        table.setServiceStatus(ServiceStatus.EMPTY);
        tablesRepository.save(table);

        // 5. Gửi thông báo WebSocket
        notifyCustomerTable(tableId, "PAYMENT_SUCCESS", "Thanh toán thành công! Cảm ơn quý khách.");
        notifyStaffAndKitchen(tableId, "CHECKOUT_COMPLETED", "Bàn " + tableId + " đã hoàn tất thanh toán tiền mặt.");
    }

    /**
     * Hủy toàn bộ hóa đơn của bàn (Xử lý cho nút [Hủy hóa đơn])
     */
    @Override
    @Transactional
    public void cancelTableOrder(Long tableId, String reason) {
        TableOrder activeOrder = tableOrderRepository.findByTableTableIdAndStatus(tableId, StatusTableOrder.OPEN)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Bàn không có hóa đơn nào đang mở để hủy!"));

        // 1. Cập nhật trạng thái đơn hàng sang CANCELLED
        activeOrder.setStatus(StatusTableOrder.CANCELLED);
        tableOrderRepository.save(activeOrder);

        // 2. Cập nhật trạng thái tất cả các món trong đơn sang CANCELLED
        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(activeOrder.getTableOrderId());
        for (OrderDetail detail : details) {
            detail.setStatus(StatusOrderDetail.CANCELLED);
            detail.setNote("Hủy đơn: " + reason);
        }
        orderDetailRepository.saveAll(details);

        // 3. Reset bàn về trạng thái TRỐNG
        Tables table = activeOrder.getTable();
        table.setServiceStatus(ServiceStatus.EMPTY);
        tablesRepository.save(table);

        // 4. Gửi thông báo WebSocket
        notifyCustomerTable(tableId, "ORDER_CANCELLED", "Hóa đơn đã bị hủy bởi nhân viên. Lý do: " + reason);
        notifyStaffAndKitchen(tableId, "ORDER_CANCELLED", "Hóa đơn bàn " + tableId + " đã bị hủy.");
    }

    @Override
    @Transactional
    public void updateTableServiceStatus(Long tableId, ServiceStatus status) {
        Tables table = getTableInfo(tableId);
        table.setServiceStatus(status);
        tablesRepository.save(table);

        notifyStaffAndKitchen(tableId, "TABLE_STATUS_CHANGED", "Bàn " + tableId + " đổi trạng thái sang " + status);
    }

    // ==========================================
    // III. THAO TÁC CHI TIẾT TỪNG MÓN (ORDER DETAIL)
    // ==========================================

    @Override
    @Transactional
    public void confirmOrderItems(Long tableOrderId) {
        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(tableOrderId);
        if (details.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Đơn hàng không có món nào!");
        }

        Long tableId = details.get(0).getOrder().getTable().getTableId();

        for (OrderDetail detail : details) {
            if (detail.getStatus() == StatusOrderDetail.ORDERED) {
                detail.setStatus(StatusOrderDetail.CONFIRMED);
            }
        }

        notifyCustomerTable(tableId, "ORDER_CONFIRMED", "Đơn hàng của bạn đã được bếp xác nhận và đang chế biến.");
        notifyStaffAndKitchen(tableId, "ORDER_CONFIRMED", "Bàn " + tableId + " đã được xác nhận đơn món.");
    }

    @Override
    @Transactional
    public void markItemAsServed(Long orderDetailId) {
        OrderDetail detail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy chi tiết đơn hàng!"));

        detail.setStatus(StatusOrderDetail.SERVED);

        Long tableId = detail.getOrder().getTable().getTableId();
        String itemName = detail.getItem().getItemName();

        notifyCustomerTable(tableId, "ITEM_SERVED", "Món '" + itemName + "' đã được phục vụ lên bàn.");
    }

    @Override
    @Transactional
    public void cancelOrderItem(Long orderDetailId, String reason) {
        OrderDetail detail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy chi tiết đơn hàng!"));

        String cancelReason = (reason != null && !reason.trim().isEmpty()) ? reason : "Hết món / Hết nguyên liệu";
        detail.setStatus(StatusOrderDetail.CANCELLED);
        detail.setNote("Đã hủy: " + cancelReason);

        TableOrder order = detail.getOrder();
        recalculateOrderTotal(order);

        Long tableId = order.getTable().getTableId();
        String itemName = detail.getItem().getItemName();

        notifyCustomerTable(tableId, "ITEM_CANCELLED", "Món '" + itemName + "' bị hủy do: " + cancelReason);
    }

    @Override
    @Transactional
    public void updateOrderItem(Long orderDetailId, Integer newQuantity, String newNote) {
        OrderDetail detail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy chi tiết đơn hàng!"));

        if (detail.getStatus() != StatusOrderDetail.ORDERED) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "Không thể sửa món đã được xác nhận hoặc đã chế biến!");
        }

        if (newQuantity <= 0) {
            deleteOrderItem(orderDetailId);
            return;
        }

        detail.setQuantity(newQuantity);
        if (newNote != null) {
            detail.setNote(newNote);
        }

        TableOrder order = detail.getOrder();
        recalculateOrderTotal(order);

        Long tableId = order.getTable().getTableId();
        notifyCustomerTable(tableId, "ITEM_UPDATED",
                "Món '" + detail.getItem().getItemName() + "' đã được thay đổi số lượng thành " + newQuantity);
    }

    @Override
    @Transactional
    public void deleteOrderItem(Long orderDetailId) {
        OrderDetail detail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy chi tiết đơn hàng!"));

        if (detail.getStatus() != StatusOrderDetail.ORDERED) {
            throw new AppException(HttpStatus.FORBIDDEN,
                    "Không thể xóa món đã được bếp xác nhận!");
        }

        TableOrder order = detail.getOrder();
        String itemName = detail.getItem().getItemName();
        Long tableId = order.getTable().getTableId();

        orderDetailRepository.delete(detail);
        recalculateOrderTotal(order);

        notifyCustomerTable(tableId, "ITEM_DELETED", "Món '" + itemName + "' đã được bỏ khỏi đơn hàng.");
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private void recalculateOrderTotal(TableOrder order) {
        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(order.getTableOrderId());

        BigDecimal newTotal = details.stream()
                .filter(d -> d.getStatus() == StatusOrderDetail.ORDERED
                        || d.getStatus() == StatusOrderDetail.CONFIRMED
                        || d.getStatus() == StatusOrderDetail.SERVED)
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(newTotal);
        tableOrderRepository.save(order);
    }

    private void notifyCustomerTable(Long tableId, String type, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tableId", tableId);
        payload.put("type", type);
        payload.put("message", message);

        messagingTemplate.convertAndSend("/topic/table/" + tableId, payload);
    }

    private void notifyStaffAndKitchen(Long tableId, String type, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tableId", tableId);
        payload.put("type", type);
        payload.put("message", message);

        messagingTemplate.convertAndSend("/topic/staff-requests", payload);
    }
}