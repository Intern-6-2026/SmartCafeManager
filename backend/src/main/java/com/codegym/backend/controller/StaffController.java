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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@SuppressWarnings("null")
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

    @PutMapping("/tables/{tableId}/confirm-all")
    public ResponseEntity<Map<String, String>> confirmAllNewItemsByTable(@PathVariable Long tableId) {
        staffOrderService.confirmAllNewItemsByTable(tableId);

        notifyTableOrderUpdate(tableId, "ORDER_CONFIRMED", "Bếp đã nhận đơn hàng của bạn!");
        notifyStaffTableListUpdate();

        return ResponseEntity.ok(Map.of("message", "Đã duyệt nhận đơn lượt mới!"));
    }

    @PutMapping("/tables/{tableId}/serve-all")
    public ResponseEntity<Map<String, String>> serveAllItemsByTable(@PathVariable Long tableId) {
        staffOrderService.serveAllItemsByTable(tableId);

        notifyTableOrderUpdate(tableId, "ALL_SERVED", "Tất cả món ăn đã được phục vụ!");
        notifyStaffTableListUpdate();

        return ResponseEntity.ok(Map.of("message", "Đã hoàn thành và phục vụ tất cả món lượt này!"));
    }

    // ==========================================
    // III. XỬ LÝ ĐƠN HÀNG VÀ THANH TOÁN BÀN
    // ==========================================

    @PutMapping("/orders/{tableOrderId}/confirm")
    public ResponseEntity<Map<String, String>> confirmOrderItems(@PathVariable Long tableOrderId) {
        staffOrderService.confirmOrderItems(tableOrderId);
        notifyStaffTableListUpdate();
        return ResponseEntity.ok(Map.of("message", "Đã xác nhận đơn hàng!"));
    }

    @PostMapping("/tables/{tableId}/approve-payment")
    public ResponseEntity<Map<String, String>> approvePayment(@PathVariable Long tableId) {
        staffOrderService.approveCashPayment(tableId);

        messagingTemplate.convertAndSend(
                "/topic/tables/" + tableId + "/payment",
                Map.of(
                        "status", "PAID",
                        "message", "Thanh toán thành công! Cảm ơn quý khách."
                )
        );
        notifyStaffTableListUpdate();

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Đã duyệt thanh toán và giải phóng bàn thành công!"
        ));
    }

    @PostMapping("/tables/{tableId}/cancel")
    public ResponseEntity<Map<String, String>> cancelTableOrder(
            @PathVariable Long tableId,
            @RequestParam(required = false, defaultValue = "Nhân viên hủy đơn") String reason) {

        staffOrderService.cancelTableOrder(tableId, reason);

        notifyTableOrderUpdate(tableId, "ORDER_CANCELLED", "Đơn hàng đã bị hủy: " + reason);
        notifyStaffTableListUpdate();

        return ResponseEntity.ok(Map.of("message", "Đã hủy đơn hàng và giải phóng bàn thành công!"));
    }

    @PutMapping("/tables/{tableId}/status")
    public ResponseEntity<Map<String, String>> updateTableStatus(
            @PathVariable Long tableId,
            @RequestParam ServiceStatus status) {

        staffOrderService.updateTableServiceStatus(tableId, status);
        notifyStaffTableListUpdate();

        return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái bàn thành công!"));
    }

    // ==========================================
    // IV. THAO TÁC MÓN LẺ (KÈM TABLE_ID ĐỂ BẮN WEBSOCKET)
    // ==========================================

    @PutMapping("/tables/{tableId}/order-details/{orderDetailId}/serve")
    public ResponseEntity<Map<String, String>> markItemAsServed(
            @PathVariable Long tableId,
            @PathVariable Long orderDetailId) {
            
        staffOrderService.markItemAsServed(orderDetailId);

        messagingTemplate.convertAndSend(
                "/topic/staff/orders/item-update",
                Map.of(
                        "tableId", tableId,
                        "orderDetailId", orderDetailId,
                        "status", "SERVED",
                        "timestamp", System.currentTimeMillis()
                )
        );

        notifyTableOrderUpdate(tableId, "ITEM_SERVED", "Món ăn đã được mang lên!");
        notifyStaffTableListUpdate();

        return ResponseEntity.ok(Map.of("message", "Đã chuyển món sang SERVED!"));
    }

    @PutMapping("/tables/{tableId}/order-details/{orderDetailId}/cancel")
    public ResponseEntity<Map<String, String>> cancelOrderItem(
            @PathVariable Long tableId,
            @PathVariable Long orderDetailId,
            @RequestParam(required = false, defaultValue = "Hết món") String reason) {

        staffOrderService.cancelOrderItem(orderDetailId, reason);

        messagingTemplate.convertAndSend(
                "/topic/staff/orders/item-update",
                Map.of(
                        "tableId", tableId,
                        "orderDetailId", orderDetailId,
                        "status", "CANCELLED",
                        "reason", reason,
                        "timestamp", System.currentTimeMillis()
                )
        );

        notifyTableOrderUpdate(tableId, "ITEM_CANCELLED", "Món ăn bị hủy do: " + reason);
        notifyStaffTableListUpdate();

        return ResponseEntity.ok(Map.of("message", "Đã hủy món và cập nhật lại tổng tiền!"));
    }

    @PutMapping("/tables/{tableId}/order-details/{orderDetailId}")
    public ResponseEntity<Map<String, String>> updateOrderItem(
            @PathVariable Long tableId,
            @PathVariable Long orderDetailId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String note) {

        staffOrderService.updateOrderItem(orderDetailId, quantity, note);

        messagingTemplate.convertAndSend(
                "/topic/staff/orders/item-update",
                Map.of(
                        "tableId", tableId,
                        "orderDetailId", orderDetailId,
                        "action", "UPDATE_QUANTITY",
                        "timestamp", System.currentTimeMillis()
                )
        );

        notifyTableOrderUpdate(tableId, "ITEM_UPDATED", "Đơn hàng đã được điều chỉnh!");

        return ResponseEntity.ok(Map.of("message", "Cập nhật số lượng/ghi chú thành công!"));
    }

    @DeleteMapping("/tables/{tableId}/order-details/{orderDetailId}")
    public ResponseEntity<Map<String, String>> deleteOrderItem(
            @PathVariable Long tableId,
            @PathVariable Long orderDetailId) {
            
        staffOrderService.deleteOrderItem(orderDetailId);

        messagingTemplate.convertAndSend(
                "/topic/staff/orders/item-update",
                Map.of(
                        "tableId", tableId,
                        "orderDetailId", orderDetailId,
                        "action", "DELETE",
                        "timestamp", System.currentTimeMillis()
                )
        );

        notifyTableOrderUpdate(tableId, "ITEM_DELETED", "Đã xóa món khỏi đơn hàng!");
        notifyStaffTableListUpdate();

        return ResponseEntity.ok(Map.of("message", "Đã xóa món khỏi đơn!"));
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private void notifyTableOrderUpdate(Long tableId, String eventType, String message) {
        messagingTemplate.convertAndSend(
                "/topic/tables/" + tableId + "/orders",
                Map.of(
                        "tableId", tableId,
                        "event", eventType,
                        "message", message,
                        "timestamp", System.currentTimeMillis()
                )
        );
    }

    private void notifyStaffTableListUpdate() {
        messagingTemplate.convertAndSend(
                "/topic/staff/tables",
                Map.of(
                        "event", "REFRESH_TABLES",
                        "timestamp", System.currentTimeMillis()
                )
        );
    }
}