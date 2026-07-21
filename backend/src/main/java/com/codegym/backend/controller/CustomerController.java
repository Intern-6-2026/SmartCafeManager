package com.codegym.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codegym.backend.dto.CartResponseDTO;
import com.codegym.backend.dto.TableOrderInvoiceDTO;
import com.codegym.backend.dto.TableOrderSummaryDTO;
import com.codegym.backend.entity.Tables;
import com.codegym.backend.enums.PaymentMethod;
import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.service.CustomerOrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/customer")
@CrossOrigin("*")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerOrderService customerOrderService;

    // --- 1. QUẢN LÝ BÀN & THÔNG TIN ---
    @GetMapping("/table-info")
    public ResponseEntity<Tables> getTableInfo(@RequestParam Long tableId) {
        return ResponseEntity.ok(customerOrderService.getTableInfo(tableId));
    }

    @PostMapping("/call-service")
    public ResponseEntity<String> callService(
            @RequestParam Long tableId,
            @RequestParam ServiceStatus status) {
        customerOrderService.updateTableServiceStatus(tableId, status);
        return ResponseEntity.ok("Hệ thống đã ghi nhận yêu cầu: " + status);
    }

    // --- 2. GIỎ HÀNG TẠM (PENDING) ---
    // Lấy tổng quan giỏ hàng (Gồm món đã gọi + món giỏ tạm)
    @GetMapping("/cart/{tableId}")
    public ResponseEntity<CartResponseDTO> getCartOverview(@PathVariable Long tableId) {
        CartResponseDTO cartOverview = customerOrderService.getCartOverview(tableId);
        return ResponseEntity.ok(cartOverview);
    }

    // Thêm món vào giỏ tạm
    @PostMapping("/cart/add")
    public ResponseEntity<String> addItemToCart(
            @RequestParam Long tableId,
            @RequestParam Long itemId,
            @RequestParam Integer quantity,
            @RequestParam(required = false, defaultValue = "") String note) {
        customerOrderService.addItemToCart(tableId, itemId, quantity, note);
        return ResponseEntity.ok("Đã thêm món vào giỏ hàng tạm thời!");
    }

    // Cập nhật số lượng + ghi chú của món trong giỏ tạm
    @PutMapping("/cart/items/{itemId}")
    public ResponseEntity<String> updateCartItemDetail(
            @RequestParam Long tableId,
            @PathVariable Long itemId,
            @RequestParam(required = false) Integer quantity,
            @RequestParam(required = false) String note) {

        if (quantity != null && quantity <= 0) {
            customerOrderService.removeItemFromCart(tableId, itemId);
            return ResponseEntity.ok("Đã xóa món khỏi giỏ hàng do số lượng <= 0!");
        }

        customerOrderService.updateCartItemDetail(tableId, itemId, quantity, note);
        return ResponseEntity.ok("Cập nhật giỏ hàng thành công!");
    }

    // Xóa 1 món khỏi giỏ tạm
    @DeleteMapping("/cart/remove")
    public ResponseEntity<String> removeItemFromCart(
            @RequestParam Long tableId,
            @RequestParam Long itemId) {
        customerOrderService.removeItemFromCart(tableId, itemId);
        return ResponseEntity.ok("Đã xóa món ăn khỏi giỏ hàng!");
    }

    // Xóa sạch giỏ tạm
    @DeleteMapping("/cart/clear")
    public ResponseEntity<String> clearCart(@RequestParam Long tableId) {
        customerOrderService.clearTemporaryCart(tableId);
        return ResponseEntity.ok("Đã xóa toàn bộ món trong giỏ hàng tạm!");
    }

    // --- 3. XÁC NHẬN GỬI BẾP & QUẢN LÝ ĐƠN ---
    // Bấm [GỌI MÓN] -> Chuyển PENDING sang CONFIRMED
    @PostMapping("/confirm-order")
    public ResponseEntity<String> confirmOrder(@RequestParam Long tableId) {
        customerOrderService.confirmOrder(tableId);
        return ResponseEntity.ok("Đã gửi đơn hàng thành công xuống bếp!");
    }

    // Bếp đánh dấu đã phục vụ
    @PutMapping("/kitchen/serve-item")
    public ResponseEntity<String> markItemAsServed(@RequestParam Long orderDetailId) {
        customerOrderService.markItemAsServed(orderDetailId);
        return ResponseEntity.ok("Đã chuyển trạng thái món sang SERVED!");
    }

    // Bếp báo hết món (Hủy món)
    @PutMapping("/kitchen/cancel-item")
    public ResponseEntity<String> cancelOrderItem(
            @RequestParam Long orderDetailId,
            @RequestParam(required = false, defaultValue = "Hết món") String reason) {
        customerOrderService.cancelOrderItem(orderDetailId, reason);
        return ResponseEntity.ok("Đã hủy món thành công và tính lại tổng tiền!");
    }

    // --- 4. THANH TOÁN & HÓA ĐƠN ---
    // Khách gửi yêu cầu thanh toán
    @PostMapping("/request-checkout")
    public ResponseEntity<String> requestCheckout(
            @RequestParam Long tableId,
            @RequestParam PaymentMethod paymentMethod) {
        customerOrderService.requestCheckout(tableId, paymentMethod);
        return ResponseEntity.ok("Yêu cầu thanh toán bằng " + paymentMethod + " đã được gửi!");
    }

    // Khách xem tóm tắt hóa đơn checkout
    @GetMapping("/invoice-summary/{tableId}")
    public ResponseEntity<TableOrderSummaryDTO> getInvoiceSummary(@PathVariable Long tableId) {
        return ResponseEntity.ok(customerOrderService.getInvoiceSummaryDTO(tableId));
    }

    // Xem chi tiết hóa đơn cụ thể (Dành cho Admin/Thu ngân)
    @GetMapping("/invoice")
    public ResponseEntity<TableOrderInvoiceDTO> getCurrentInvoice(
            @RequestParam Long tableId,
            @RequestParam Long tableOrderId) {
        return ResponseEntity.ok(customerOrderService.getCurrentInvoice(tableId, tableOrderId));
    }

    // Thu ngân xác nhận đã thu tiền -> Hoàn tất checkout & Giải phóng bàn
    @PostMapping("/complete-checkout")
    public ResponseEntity<String> completeCheckout(
            @RequestParam Long tableId,
            @RequestParam PaymentMethod paymentMethod) {
        customerOrderService.completeCheckout(tableId, paymentMethod);
        return ResponseEntity.ok("Thanh toán thành công! Hóa đơn đã chốt và bàn đã giải phóng.");
    }
}