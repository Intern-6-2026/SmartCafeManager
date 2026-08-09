package com.codegym.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import com.codegym.backend.dto.CartResponseDTO;
import com.codegym.backend.dto.TableOrderSummaryDTO;
import com.codegym.backend.entity.Tables;
import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.service.OrderService;
import com.codegym.backend.service.PaymentService;
import com.codegym.backend.service.StaffOrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/customer")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final StaffOrderService staffOrderService;
    private final SimpMessagingTemplate messagingTemplate;

    // --- 1. THÔNG TIN BÀN & GỌI PHỤC VỤ ---
    @GetMapping("/table-info")
    public ResponseEntity<Tables> getTableInfo(@RequestParam Long tableId) {
        return ResponseEntity.ok(staffOrderService.getTableInfo(tableId));
    }

    @PostMapping("/call-service")
    public ResponseEntity<String> callService(
            @RequestParam Long tableId,
            @RequestParam ServiceStatus status
    ) {
        if (status != ServiceStatus.CALL_STAFF && status != ServiceStatus.REQUESTING_BILL) {
            return ResponseEntity.badRequest().body("Yêu cầu không hợp lệ!");
        }

        orderService.updateTableServiceStatus(tableId, status);

        // Bổ sung phát thông báo WebSocket tới màn hình nhân viên
        String type = (status == ServiceStatus.CALL_STAFF) ? "CALL_STAFF" : "REQUESTING_BILL";
        String message = (status == ServiceStatus.CALL_STAFF) 
                ? "Bàn " + tableId + " đang gọi nhân viên!" 
                : "Bàn " + tableId + " yêu cầu thanh toán!";
        
        sendWebSocketNotification(tableId, type, message);

        return ResponseEntity.ok(message);
    }

    // --- 2. GIỎ HÀNG TẠM ---
    @GetMapping("/cart/{tableId}")
    public ResponseEntity<CartResponseDTO> getCartOverview(@PathVariable Long tableId) {
        return ResponseEntity.ok(orderService.getCartOverview(tableId));
    }

    @PostMapping("/cart/add")
    public ResponseEntity<String> addItemToCart(
            @RequestParam Long tableId,
            @RequestParam Long itemId,
            @RequestParam Integer quantity,
            @RequestParam(required = false, defaultValue = "") String note) {
        orderService.addItemToCart(tableId, itemId, quantity, note);
        return ResponseEntity.ok("Đã thêm món vào giỏ hàng tạm!");
    }

    @PutMapping("/cart/items/{itemId}")
    public ResponseEntity<String> updateCartItemDetail(
            @RequestParam Long tableId,
            @PathVariable Long itemId,
            @RequestParam(required = false) Integer quantity,
            @RequestParam(required = false) String note) {

        if (quantity != null && quantity <= 0) {
            orderService.removeItemFromCart(tableId, itemId);
            return ResponseEntity.ok("Đã xóa món khỏi giỏ hàng!");
        }

        orderService.updateCartItemDetail(tableId, itemId, quantity, note);
        return ResponseEntity.ok("Cập nhật giỏ hàng thành công!");
    }

    @DeleteMapping("/cart/remove")
    public ResponseEntity<String> removeItemFromCart(
            @RequestParam Long tableId,
            @RequestParam Long itemId) {
        orderService.removeItemFromCart(tableId, itemId);
        return ResponseEntity.ok("Đã xóa món ăn khỏi giỏ hàng!");
    }

    @DeleteMapping("/cart/clear")
    public ResponseEntity<String> clearCart(@RequestParam Long tableId) {
        orderService.clearTemporaryCart(tableId);
        return ResponseEntity.ok("Đã xóa toàn bộ món trong giỏ hàng tạm!");
    }

    // --- 3. BẤM GỌI MÓN ---
    @PostMapping("/confirm-order")
    public ResponseEntity<String> confirmOrder(@RequestParam Long tableId) {
        orderService.confirmOrder(tableId);
        sendWebSocketNotification(tableId, "NEW_ORDER", "Bàn " + tableId + " vừa gửi đơn món mới!");
        return ResponseEntity.ok("Đã gửi đơn hàng thành công xuống bếp!");
    }

    // --- 4. YÊU CẦU THANH TOÁN ---
    @PostMapping("/payment/cash")
    public ResponseEntity<String> processCashPayment(@RequestParam Long tableId) {
        paymentService.processCashPayment(tableId);
        sendWebSocketNotification(tableId, "CASH_PAYMENT_REQUEST", "Bàn " + tableId + " yêu cầu THANH TOÁN TIỀN MẶT!");
        return ResponseEntity.ok("Đã gửi yêu cầu thanh toán tiền mặt. Vui lòng chờ nhân viên!");
    }

    @GetMapping("/invoice-summary/{tableId}")
    public ResponseEntity<TableOrderSummaryDTO> getInvoiceSummary(@PathVariable Long tableId) {
        return ResponseEntity.ok(paymentService.getInvoiceSummaryDTO(tableId));
    }

    // Helper WebSocket an toàn - Bọc try-catch tránh làm crash API (HTTP 500) khi socket lỗi
    private void sendWebSocketNotification(Long tableId, String type, String message) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("tableId", tableId);
            payload.put("type", type);
            payload.put("message", message);
            messagingTemplate.convertAndSend("/topic/staff-requests", payload);
        } catch (Exception e) {
            log.error("Lỗi gửi WebSocket tới /topic/staff-requests: {}", e.getMessage());
        }
    }
}