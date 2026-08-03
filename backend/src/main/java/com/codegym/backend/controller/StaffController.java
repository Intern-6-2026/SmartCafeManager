package com.codegym.backend.controller;

import com.codegym.backend.entity.OrderDetail;
import com.codegym.backend.entity.Tables;
import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.service.StaffOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/staff")
@CrossOrigin("*")
@RequiredArgsConstructor
public class StaffController {

    private final StaffOrderService staffOrderService;

    // ==========================================
    // I. SƠ ĐỒ BÀN & CHI TIẾT SIDE-PANEL
    // ==========================================


    //  * 1. Lấy danh sách tất cả các bàn để vẽ sơ đồ
    @GetMapping("/tables")
    public ResponseEntity<List<Tables>> getAllTables() {
        return ResponseEntity.ok(staffOrderService.getAllTables());
    }


    //  * 2. Lấy thông tin bàn cụ thể

    @GetMapping("/tables/{tableId}")
    public ResponseEntity<Tables> getTableInfo(@PathVariable Long tableId) {
        return ResponseEntity.ok(staffOrderService.getTableInfo(tableId));
    }


    //  * 3. Lấy chi tiết đơn hàng đang active của bàn (Hiển thị Sidepanel bên phải khi click chọn bàn)

    @GetMapping("/tables/{tableId}/active-order")
    public ResponseEntity<?> getActiveOrderByTable(@PathVariable Long tableId) {
        return ResponseEntity.ok(staffOrderService.getActiveOrderByTable(tableId));
    }


    //  * 4. Lấy danh sách món ăn chi tiết của bàn (Nếu muốn query món lẻ)

    @GetMapping("/tables/{tableId}/order-details")
    public ResponseEntity<List<OrderDetail>> getOrderDetailsByTable(@PathVariable Long tableId) {
        return ResponseEntity.ok(staffOrderService.getOrderDetailsByTable(tableId));
    }

    // ==========================================
    // II. THAO TÁC XÁC NHẬN & CHỐT ĐƠN
    // ==========================================


    //  * 5. Nút [Xác nhận thanh toán]: Thu ngân / Phục vụ duyệt thanh toán (Chốt đơn & Giải phóng bàn)

    @PostMapping("/tables/{tableId}/approve-payment")
    public ResponseEntity<Map<String, String>> approvePayment(@PathVariable Long tableId) {
        staffOrderService.approveCashPayment(tableId);
        return ResponseEntity.ok(Map.of("message", "Đã duyệt thanh toán và giải phóng bàn!"));
    }


    //  * 6. Nút [Hủy hóa đơn]: Hủy toàn bộ đơn hàng của bàn
    @PostMapping("/tables/{tableId}/cancel-order")
    public ResponseEntity<Map<String, String>> cancelOrder(
            @PathVariable Long tableId,
            @RequestParam(required = false, defaultValue = "Khách đổi ý hủy đơn") String reason) {
        staffOrderService.cancelTableOrder(tableId, reason);
        return ResponseEntity.ok(Map.of("message", "Đã hủy hóa đơn thành công!"));
    }


    //  * 7. Cập nhật trạng thái dọn dẹp / phục vụ của bàn (Trống, Gọi nhân viên, Chờ tính tiền...)

    @PutMapping("/tables/{tableId}/status")
    public ResponseEntity<Map<String, String>> updateTableStatus(
            @PathVariable Long tableId,
            @RequestParam ServiceStatus status) {
        staffOrderService.updateTableServiceStatus(tableId, status);
        return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái bàn thành công!"));
    }

    // ==========================================
    // III. THAO TÁC TỪNG MÓN ĂN (ORDER DETAIL)
    // ==========================================


    //  * 8. Bếp / Nhân viên bấm XÁC NHẬN ĐƠN MÓN (ORDERED -> CONFIRMED)

    @PutMapping("/orders/{tableOrderId}/confirm")
    public ResponseEntity<Map<String, String>> confirmOrderItems(@PathVariable Long tableOrderId) {
        staffOrderService.confirmOrderItems(tableOrderId);
        return ResponseEntity.ok(Map.of("message", "Đã xác nhận đơn hàng thành công!"));
    }


    //  * 9. Bếp/Phục vụ đánh dấu món ĐÃ RA BÀN (CONFIRMED -> SERVED)

    @PutMapping("/order-details/{orderDetailId}/serve")
    public ResponseEntity<Map<String, String>> markItemAsServed(@PathVariable Long orderDetailId) {
        staffOrderService.markItemAsServed(orderDetailId);
        return ResponseEntity.ok(Map.of("message", "Đã chuyển trạng thái món sang SERVED!"));
    }


    //  * 10. HỦY MÓN lẻ (Do hết món/hết nguyên liệu)

    @PutMapping("/order-details/{orderDetailId}/cancel")
    public ResponseEntity<Map<String, String>> cancelOrderItem(
            @PathVariable Long orderDetailId,
            @RequestParam(required = false, defaultValue = "Hết món") String reason) {
        staffOrderService.cancelOrderItem(orderDetailId, reason);
        return ResponseEntity.ok(Map.of("message", "Đã hủy món thành công và cập nhật lại tổng tiền!"));
    }


    //  * 11. SỬA MÓN (Sửa số lượng / ghi chú khi món đang ORDERED)

    @PutMapping("/order-details/{orderDetailId}")
    public ResponseEntity<Map<String, String>> updateOrderItem(
            @PathVariable Long orderDetailId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String note) {
        staffOrderService.updateOrderItem(orderDetailId, quantity, note);
        return ResponseEntity.ok(Map.of("message", "Cập nhật món thành công!"));
    }


    //  * 12. XÓA MÓN khỏi đơn (khi món đang ORDERED)

    @DeleteMapping("/order-details/{orderDetailId}")
    public ResponseEntity<Map<String, String>> deleteOrderItem(@PathVariable Long orderDetailId) {
        staffOrderService.deleteOrderItem(orderDetailId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa món khỏi đơn hàng!"));
    }
}