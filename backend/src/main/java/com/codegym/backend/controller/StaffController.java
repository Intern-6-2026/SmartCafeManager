package com.codegym.backend.controller;

import com.codegym.backend.dto.ActiveOrderDTO;
import com.codegym.backend.dto.OrderDetailResponseDTO;
import com.codegym.backend.entity.Tables;
import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.service.StaffOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/staff")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class StaffController {

    private final StaffOrderService staffOrderService;
    private final SimpMessagingTemplate messagingTemplate;

    // ==========================================
    // I. LẤY SƠ ĐỒ BÀN & THÔNG TIN ĐƠN HÀNG
    // ==========================================

    @GetMapping("/tables")
    public ResponseEntity<List<Tables>> getAllTables() {
        return ResponseEntity.ok(staffOrderService.getAllTables());
    }

    @GetMapping("/tables/{tableId}")
    public ResponseEntity<Tables> getTableInfo(@PathVariable Long tableId) {
        return ResponseEntity.ok(staffOrderService.getTableInfo(tableId));
    }

    // 🟢 CẬP NHẬT: Đổi từ Object sang ActiveOrderDTO cho rõ ràng dữ liệu
    @GetMapping("/tables/{tableId}/active-order")
    public ResponseEntity<ActiveOrderDTO> getActiveOrderByTable(@PathVariable Long tableId) {
        return ResponseEntity.ok(staffOrderService.getActiveOrderByTable(tableId));
    }

    @GetMapping("/tables/{tableId}/order-details")
    public ResponseEntity<List<OrderDetailResponseDTO>> getOrderDetailsByTable(@PathVariable Long tableId) {
        return ResponseEntity.ok(staffOrderService.getOrderDetailsByTable(tableId));
    }

    // ==========================================
    // II. XỬ LÝ HÀNG LOẠT THEO LƯỢT ORDER CỦA BÀN
    // ==========================================

    /**
     * Bếp nhận nấu tất cả món mới vừa đặt của bàn (ORDERED -> CONFIRMED)
     */
    @PutMapping("/tables/{tableId}/confirm-all")
    public ResponseEntity<Map<String, String>> confirmAllNewItemsByTable(@PathVariable Long tableId) {
        staffOrderService.confirmAllNewItemsByTable(tableId);
        notifyTableUpdate(tableId, "ITEMS_CONFIRMED", "Bếp đã nhận chế biến tất cả món mới của bàn " + tableId);
        return ResponseEntity.ok(Map.of("message", "Đã duyệt nhận đơn lượt mới!"));
    }

    /**
     * Phục vụ/Bếp chốt hoàn thành tất cả món đang làm lượt này ra bàn (CONFIRMED -> SERVED)
     */
    @PutMapping("/tables/{tableId}/serve-all")
    public ResponseEntity<Map<String, String>> serveAllItemsByTable(@PathVariable Long tableId) {
        staffOrderService.serveAllItemsByTable(tableId);
        notifyTableUpdate(tableId, "ALL_ITEMS_SERVED", "Bàn " + tableId + " đã phục vụ xong tất cả món lượt này!");
        return ResponseEntity.ok(Map.of("message", "Đã hoàn thành và phục vụ tất cả món lượt này!"));
    }

    // ==========================================
    // III. XỬ LÝ ĐƠN HÀNG VÀ THANH TOÁN BÀN
    // ==========================================

    @PutMapping("/orders/{tableOrderId}/confirm")
    public ResponseEntity<Map<String, String>> confirmOrderItems(@PathVariable Long tableOrderId) {
        staffOrderService.confirmOrderItems(tableOrderId);
        return ResponseEntity.ok(Map.of("message", "Đã xác nhận đơn hàng!"));
    }

    @PostMapping("/tables/{tableId}/approve-payment")
    public ResponseEntity<Map<String, String>> approvePayment(@PathVariable Long tableId) {
        staffOrderService.approveCashPayment(tableId);
        notifyTableUpdate(tableId, "PAYMENT_APPROVED", "Bàn " + tableId + " đã thanh toán và hoàn tất!");
        return ResponseEntity.ok(Map.of("message", "Đã duyệt thanh toán và giải phóng bàn!"));
    }

    // 🟢 BỔ SUNG: Endpoint Hủy toàn bộ đơn hàng của bàn khi cần
    @PostMapping("/tables/{tableId}/cancel")
    public ResponseEntity<Map<String, String>> cancelTableOrder(
            @PathVariable Long tableId,
            @RequestParam(required = false, defaultValue = "Nhân viên hủy đơn") String reason) {

        staffOrderService.cancelTableOrder(tableId, reason);
        notifyTableUpdate(tableId, "ORDER_CANCELLED", "Đơn hàng bàn " + tableId + " đã bị hủy.");
        return ResponseEntity.ok(Map.of("message", "Đã hủy đơn hàng và giải phóng bàn thành công!"));
    }

    @PutMapping("/tables/{tableId}/status")
    public ResponseEntity<Map<String, String>> updateTableStatus(
            @PathVariable Long tableId,
            @RequestParam ServiceStatus status) {

        staffOrderService.updateTableServiceStatus(tableId, status);
        notifyTableUpdate(tableId, "STATUS_CHANGED", "Bàn " + tableId + " chuyển trạng thái: " + status.name());
        return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái bàn thành công!"));
    }

    // ==========================================
    // IV. THAO TÁC MÓN LẺ (KHI CẦN ĐIỀU CHỈNH ĐẶC BIỆT)
    // ==========================================

    @PutMapping("/order-details/{orderDetailId}/serve")
    public ResponseEntity<Map<String, String>> markItemAsServed(@PathVariable Long orderDetailId) {
        staffOrderService.markItemAsServed(orderDetailId);
        return ResponseEntity.ok(Map.of("message", "Đã chuyển món sang SERVED!"));
    }

    @PutMapping("/order-details/{orderDetailId}/cancel")
    public ResponseEntity<Map<String, String>> cancelOrderItem(
            @PathVariable Long orderDetailId,
            @RequestParam(required = false, defaultValue = "Hết món") String reason) {

        staffOrderService.cancelOrderItem(orderDetailId, reason);
        return ResponseEntity.ok(Map.of("message", "Đã hủy món và cập nhật lại tổng tiền!"));
    }

    @PutMapping("/order-details/{orderDetailId}")
    public ResponseEntity<Map<String, String>> updateOrderItem(
            @PathVariable Long orderDetailId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String note) {

        staffOrderService.updateOrderItem(orderDetailId, quantity, note);
        return ResponseEntity.ok(Map.of("message", "Cập nhật số lượng/ghi chú thành công!"));
    }

    @DeleteMapping("/order-details/{orderDetailId}")
    public ResponseEntity<Map<String, String>> deleteOrderItem(@PathVariable Long orderDetailId) {
        staffOrderService.deleteOrderItem(orderDetailId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa món khỏi đơn!"));
    }

    // --- WEBSOCKET HELPER ---
    private void notifyTableUpdate(Long tableId, String type, String message) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("tableId", tableId);
            payload.put("type", type);
            payload.put("message", message);

            messagingTemplate.convertAndSend("/topic/table-events", payload);
        } catch (Exception e) {
            log.error("Lỗi gửi tin nhắn WebSocket cho bàn {}: {}", tableId, e.getMessage());
        }
    }
}