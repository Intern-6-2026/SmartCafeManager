package com.codegym.backend.controller;

import com.codegym.backend.dto.CartItemRequestDTO;
import com.codegym.backend.dto.CartResponseDTO;
import com.codegym.backend.dto.TableOrderSummaryDTO;
import com.codegym.backend.entity.Tables;
import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.service.CartService;
import com.codegym.backend.service.OrderService;
import com.codegym.backend.service.PaymentService;
import com.codegym.backend.service.StaffOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final StaffOrderService staffOrderService;
    private final SimpMessagingTemplate messagingTemplate;
    // I. THÔNG TIN BÀN & GỌI PHỤC VỤ
    @GetMapping("/table-info/{tableId}")
    public ResponseEntity<Tables> getTableInfo(@PathVariable Long tableId) {
        return ResponseEntity.ok(staffOrderService.getTableInfo(tableId));
    }

    @PostMapping("/call-service")
    public ResponseEntity<Map<String, String>> callService(
            @RequestParam Long tableId,
            @RequestParam ServiceStatus status
    ) {
        if (status != ServiceStatus.CALL_STAFF && status != ServiceStatus.REQUESTING_BILL) {
            return ResponseEntity.badRequest().body(Map.of("message", "Yêu cầu không hợp lệ!"));
        }

        orderService.updateTableServiceStatus(tableId, status);

        String type = (status == ServiceStatus.CALL_STAFF) ? "CALL_STAFF" : "REQUESTING_BILL";
        String message = (status == ServiceStatus.CALL_STAFF) 
                ? "Bàn " + tableId + " đang gọi nhân viên!" 
                : "Bàn " + tableId + " yêu cầu thanh toán!";
        
        // Báo cho Nhân viên/Admin/Bếp VÀ Khách hàng tại bàn
        notifyStaffAndKitchen(tableId, type, message);
        notifyCustomerTable(tableId, type, message);

        return ResponseEntity.ok(Map.of("message", message));
    }
    // II. GIỎ HÀNG TẠM
    @GetMapping("/cart/{tableId}")
    public ResponseEntity<CartResponseDTO> getCartOverview(@PathVariable Long tableId) {
        return ResponseEntity.ok(cartService.getCartOverview(tableId));
    }

    @PostMapping("/cart/add")
    public ResponseEntity<Map<String, String>> addItemToCart(@Valid @RequestBody CartItemRequestDTO dto) {
        cartService.addItemToCart(dto.getTableId(), dto.getItemId(), dto.getQuantity(), dto.getNote());
        
        notifyCustomerTable(dto.getTableId(), "CART_UPDATED", "Giỏ hàng vừa được cập nhật!");
        return ResponseEntity.ok(Map.of("message", "Đã thêm món vào giỏ hàng tạm!"));
    }

    @PutMapping("/cart/items/{itemId}")
    public ResponseEntity<Map<String, String>> updateCartItemDetail(
            @RequestParam Long tableId,
            @PathVariable Long itemId,
            @RequestParam(required = false) Integer quantity,
            @RequestParam(required = false) String note) {

        if (quantity != null && quantity <= 0) {
            cartService.removeItemFromCart(tableId, itemId);
            notifyCustomerTable(tableId, "CART_UPDATED", "Đã xóa món khỏi giỏ hàng!");
            return ResponseEntity.ok(Map.of("message", "Đã xóa món khỏi giỏ hàng!"));
        }

        cartService.updateCartItemDetail(tableId, itemId, quantity, note);
        notifyCustomerTable(tableId, "CART_UPDATED", "Giỏ hàng đã thay đổi!");
        return ResponseEntity.ok(Map.of("message", "Cập nhật giỏ hàng thành công!"));
    }

    @DeleteMapping("/cart/items/{itemId}")
    public ResponseEntity<Map<String, String>> removeItemFromCart(
            @RequestParam Long tableId,
            @PathVariable Long itemId) {
        cartService.removeItemFromCart(tableId, itemId);
        notifyCustomerTable(tableId, "CART_UPDATED", "Đã xóa món khỏi giỏ hàng!");
        return ResponseEntity.ok(Map.of("message", "Đã xóa món ăn khỏi giỏ hàng!"));
    }

    @DeleteMapping("/cart/clear")
    public ResponseEntity<Map<String, String>> clearCart(@RequestParam Long tableId) {
        cartService.clearTemporaryCart(tableId);
        notifyCustomerTable(tableId, "CART_UPDATED", "Giỏ hàng đã bị xóa sạch!");
        return ResponseEntity.ok(Map.of("message", "Đã xóa toàn bộ món trong giỏ hàng tạm!"));
    }

    // III. BẤM GỌI MÓN (ĐỐI CHIẾU CHUẨN CẢ SERVICE)
    @PostMapping("/confirm-order")
    public ResponseEntity<Map<String, String>> confirmOrder(@RequestParam Long tableId) {
        // 🟢 Trong CartServiceImpl.confirmOrder() đã tích hợp bắn Socket NEW_ORDER_SUBMITTED 
        // cho cả /topic/table-events và /topic/table/{tableId} nên không bắn lại ở đây để tránh trùng lặp.
        cartService.confirmOrder(tableId);

        return ResponseEntity.ok(Map.of("message", "Đã gửi đơn hàng thành công xuống bếp!"));
    }

    // IV. YÊU CẦU THANH TOÁN & HÓA ĐƠN

    @PostMapping("/payment/cash")
    public ResponseEntity<Map<String, String>> processCashPayment(@RequestParam Long tableId) {
        paymentService.processCashPayment(tableId);
        
        notifyStaffAndKitchen(tableId, "CASH_PAYMENT_REQUEST", "Bàn " + tableId + " yêu cầu THANH TOÁN TIỀN MẶT!");
        notifyCustomerTable(tableId, "WAITING_PAYMENT", "Đã gửi yêu cầu, vui lòng chờ nhân viên tới thu tiền mặt!");

        return ResponseEntity.ok(Map.of("message", "Đã gửi yêu cầu thanh toán tiền mặt. Vui lòng chờ nhân viên!"));
    }

    @GetMapping("/invoice-summary/{tableId}")
    public ResponseEntity<TableOrderSummaryDTO> getInvoiceSummary(@PathVariable Long tableId) {
        return ResponseEntity.ok(paymentService.getInvoiceSummaryDTO(tableId));
    }
    // HELPER WEBSOCKET (ĐỒNG BỘ 1 KÊNH TABLE-EVENTS)
    private void notifyStaffAndKitchen(Long tableId, String type, String message) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("tableId", tableId);
            payload.put("type", type);
            payload.put("message", message);
            payload.put("timestamp", System.currentTimeMillis());
            
            // Đã chuyển chuẩn kênh chung: /topic/table-events
            messagingTemplate.convertAndSend("/topic/table-events", payload);
        } catch (Exception e) {
            log.error("Lỗi gửi WebSocket tới /topic/table-events: {}", e.getMessage());
        }
    }

    private void notifyCustomerTable(Long tableId, String type, String message) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("tableId", tableId);
            payload.put("type", type);
            payload.put("message", message);
            payload.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/table/" + tableId, payload);
        } catch (Exception e) {
            log.error("Lỗi gửi WebSocket tới /topic/table/{}: {}", tableId, e.getMessage());
        }
    }
}