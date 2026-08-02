package com.codegym.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.codegym.backend.entity.OrderDetail;
import com.codegym.backend.entity.Tables;
import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.service.StaffOrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/staff")
@CrossOrigin("*")
@RequiredArgsConstructor
public class StaffController {

    private final StaffOrderService staffOrderService;
    // 1. Xem thông tin cơ bản của bàn
    @GetMapping("/table/{tableId}")
    public ResponseEntity<Tables> getTableInfo(@PathVariable Long tableId) {
        return ResponseEntity.ok(staffOrderService.getTableInfo(tableId));
    }
    // 2. Xem chi tiết danh sách món ăn đang gọi của bàn
    @GetMapping("/table/{tableId}/order-details")
    public ResponseEntity<List<OrderDetail>> getOrderDetailsByTable(@PathVariable Long tableId) {
        return ResponseEntity.ok(staffOrderService.getOrderDetailsByTable(tableId));
    }
    // 3. Nhân viên / Bếp bấm XÁC NHẬN ĐƠN MÓN (Chuyển ORDERED -> CONFIRMED)
    @PutMapping("/confirm-order")
    public ResponseEntity<String> confirmOrderItems(@RequestParam Long tableOrderId) {
        staffOrderService.confirmOrderItems(tableOrderId);
        return ResponseEntity.ok("Đã xác nhận đơn hàng thành công!");
    }
    // 4. Bếp/Phục vụ đánh dấu món ĐÃ RA BÀN (CONFIRMED -> SERVED)
    @PutMapping("/serve-item")
    public ResponseEntity<String> markItemAsServed(@RequestParam Long orderDetailId) {
        staffOrderService.markItemAsServed(orderDetailId);
        return ResponseEntity.ok("Đã chuyển trạng thái món sang SERVED!");
    }
    // 5. Bếp/Nhân viên HỦY MÓN (Hết món / Hết nguyên liệu) kèm lý do
    @PutMapping("/cancel-item")
    public ResponseEntity<String> cancelOrderItem(
            @RequestParam Long orderDetailId,
            @RequestParam(required = false, defaultValue = "Hết món") String reason) {
        staffOrderService.cancelOrderItem(orderDetailId, reason);
        return ResponseEntity.ok("Đã hủy món thành công và cập nhật lại tổng tiền!");
    }
    // 6. SỬA MÓN (Sửa số lượng / ghi chú - chỉ áp dụng khi món đang ở trạng thái ORDERED)
    @PutMapping("/update-item")
    public ResponseEntity<String> updateOrderItem(
            @RequestParam Long orderDetailId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) String note) {
        staffOrderService.updateOrderItem(orderDetailId, quantity, note);
        return ResponseEntity.ok("Cập nhật món thành công!");
    }

    // 7. XÓA MÓN khỏi đơn (Chỉ áp dụng khi món đang ở trạng thái ORDERED)
    @DeleteMapping("/delete-item/{orderDetailId}")
    public ResponseEntity<String> deleteOrderItem(@PathVariable Long orderDetailId) {
        staffOrderService.deleteOrderItem(orderDetailId);
        return ResponseEntity.ok("Đã xóa món khỏi đơn hàng!");
    }
    // 8. Thu ngân XÁC NHẬN THANH TOÁN TIỀN MẶT (Chốt đơn & Giải phóng bàn)
    @PostMapping("/approve-cash-payment")
    public ResponseEntity<String> approveCashPayment(@RequestParam Long tableId) {
        staffOrderService.approveCashPayment(tableId);
        return ResponseEntity.ok("Đã duyệt thanh toán tiền mặt & giải phóng bàn thành công!");
    }
    // 9. Cập nhật trạng thái phục vụ / dọn dẹp của bàn
    @PutMapping("/table-status")
    public ResponseEntity<String> updateTableStatus(
            @RequestParam Long tableId,
            @RequestParam ServiceStatus status) {
        staffOrderService.updateTableServiceStatus(tableId, status);
        return ResponseEntity.ok("Cập nhật trạng thái bàn thành công!");
    }
}