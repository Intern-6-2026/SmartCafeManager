package com.codegym.backend.service;

import com.codegym.backend.entity.OrderDetail;
import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.entity.Tables;
import com.codegym.backend.enums.PaymentMethod;
import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.enums.StatusOrderDetail;
import com.codegym.backend.enums.StatusTableOrder;
import com.codegym.backend.repository.OrderDetailRepository;
import com.codegym.backend.repository.TableOrderRepository;
import com.codegym.backend.repository.TablesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class StaffOrderServiceImpl implements StaffOrderService {

    private final TablesRepository tablesRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final TableOrderRepository tableOrderRepository;
    private final PaymentService paymentService; // Dùng để chốt checkout
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<OrderDetail> getOrderDetailsByTable(Long tableId) {
        // 🟢 Dùng StatusTableOrder.UNPAID (hoặc trạng thái tương ứng của đơn hàng)
        TableOrder activeOrder = tableOrderRepository.findByTableTableIdAndStatus(tableId, StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Bàn " + tableId + " hiện không có đơn hàng nào đang hoạt động!"));
                
        return orderDetailRepository.findByOrderTableOrderId(activeOrder.getTableOrderId());
    }

    // --- 2. XÁC NHẬN ĐƠN MÓN (ORDERED -> CONFIRMED) ---
    @Override
    @Transactional
    public void confirmOrderItems(Long tableOrderId) {
        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(tableOrderId);
        if (details.isEmpty()) {
            throw new RuntimeException("Đơn hàng không có món nào!");
        }

        Long tableId = details.get(0).getOrder().getTable().getTableId();

        // Chuyển tất cả món đang ORDERED sang CONFIRMED
        for (OrderDetail detail : details) {
            if (detail.getStatus() == StatusOrderDetail.ORDERED) {
                detail.setStatus(StatusOrderDetail.CONFIRMED);
            }
        }

        // 📡 Bắn Socket cho Khách hàng tại bàn & Bếp
        notifyCustomerTable(tableId, "ORDER_CONFIRMED", "Đơn hàng của bạn đã được bếp xác nhận và đang chế biến.");
        notifyStaffAndKitchen(tableId, "ORDER_CONFIRMED", "Bàn " + tableId + " đã được xác nhận đơn món.");
    }

    // --- 3. ĐỔI TRẠNG THÁI SANG SERVED (ĐÃ LÊN MÓN) ---
    @Override
    @Transactional
    public void markItemAsServed(Long orderDetailId) {
        OrderDetail detail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết đơn hàng!"));

        detail.setStatus(StatusOrderDetail.SERVED);

        Long tableId = detail.getOrder().getTable().getTableId();
        String itemName = detail.getItem().getItemName();

        // 📡 Bắn Socket báo cho khách hàng món đã lên bàn
        notifyCustomerTable(tableId, "ITEM_SERVED", "Món '" + itemName + "' đã được phục vụ lên bàn.");
    }

    // --- 4. HỦY MÓN KÈM LÝ DO (CANCELLED) ---
    @Override
    @Transactional
    public void cancelOrderItem(Long orderDetailId, String reason) {
        OrderDetail detail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết đơn hàng!"));

        String cancelReason = (reason != null && !reason.trim().isEmpty()) ? reason : "Hết món / Hết nguyên liệu";
        detail.setStatus(StatusOrderDetail.CANCELLED);
        detail.setNote("Đã hủy: " + cancelReason);

        TableOrder order = detail.getOrder();
        recalculateOrderTotal(order);

        Long tableId = order.getTable().getTableId();
        String itemName = detail.getItem().getItemName();

        // 📡 Bắn Socket báo khách hàng món bị hủy kèm lý do
        notifyCustomerTable(tableId, "ITEM_CANCELLED", 
                "Món '" + itemName + "' bị hủy do: " + cancelReason);
    }

    // --- 5. SỬA MÓN (CHỈ KHI TRẠNG THÁI == ORDERED) ---
    @Override
    @Transactional
    public void updateOrderItem(Long orderDetailId, Integer newQuantity, String newNote) {
        OrderDetail detail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết đơn hàng!"));

        if (detail.getStatus() != StatusOrderDetail.ORDERED) {
            throw new RuntimeException("Không thể sửa món đã được xác nhận hoặc đã chế biến!");
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

    // --- 6. XÓA MÓN KHI ĐANG ORDERED ---
    @Override
    @Transactional
    public void deleteOrderItem(Long orderDetailId) {
        OrderDetail detail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết đơn hàng!"));

        if (detail.getStatus() != StatusOrderDetail.ORDERED) {
            throw new RuntimeException("Không thể xóa món đã được bếp xác nhận!");
        }

        TableOrder order = detail.getOrder();
        String itemName = detail.getItem().getItemName();
        Long tableId = order.getTable().getTableId();

        orderDetailRepository.delete(detail);
        recalculateOrderTotal(order);

        notifyCustomerTable(tableId, "ITEM_DELETED", "Món '" + itemName + "' đã được bỏ khỏi đơn hàng.");
    }

    // --- 7. DUYỆT THANH TOÁN TIỀN MẶT ---
    @Override
    @Transactional
    public void approveCashPayment(Long tableId) {
        // 1. Chốt đơn hàng & đổi trạng thái bàn trong DB
        paymentService.completeCheckout(tableId, PaymentMethod.CASH);

        // 2. 📡 Bắn Socket thông báo cho Khách hàng tại bàn biết đã thu tiền xong
        notifyCustomerTable(tableId, "PAYMENT_SUCCESS", "Thanh toán tiền mặt thành công! Cảm ơn quý khách.");

        // 3. 📡 Bắn Socket thông báo cho toàn bộ Nhân viên/Thu ngân để cập nhật sơ đồ bàn
        notifyStaffAndKitchen(tableId, "CHECKOUT_COMPLETED", "Bàn " + tableId + " đã hoàn tất thanh toán tiền mặt.");
    }

    @Override
    @Transactional(readOnly = true)
    public Tables getTableInfo(Long tableId) {
        return tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));
    }

    @Override
    @Transactional
    public void updateTableServiceStatus(Long tableId, ServiceStatus status) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));
        table.setServiceStatus(status);

        notifyStaffAndKitchen(tableId, "TABLE_STATUS_CHANGED", "Bàn " + tableId + " đổi trạng thái sang " + status);
    }

    // --- HELPER METHODS (TÍNH TỔNG TIỀN & BẮN WEBSOCKET) ---

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

    // Gửi Socket riêng cho Khách hàng đang ngồi ở bàn này
    private void notifyCustomerTable(Long tableId, String type, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tableId", tableId);
        payload.put("type", type);
        payload.put("message", message);

        messagingTemplate.convertAndSend("/topic/table/" + tableId, payload);
    }

    // Gửi Socket chung cho Nhân viên / Thu ngân / Bếp
    private void notifyStaffAndKitchen(Long tableId, String type, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tableId", tableId);
        payload.put("type", type);
        payload.put("message", message);

        messagingTemplate.convertAndSend("/topic/staff-requests", payload);
    }
}