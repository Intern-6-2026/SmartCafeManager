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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class CustomerController {

    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final StaffOrderService staffOrderService;
    private final SimpMessagingTemplate messagingTemplate;

    // ==========================================
    // I. THÔNG TIN BÀN & GỌI PHỤC VỤ
    // ==========================================

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

        // Notify Realtime: Báo ngay lập tức lên màn hình Sơ đồ bàn của Nhân viên
        messagingTemplate.convertAndSend(
                "/topic/staff/tables",
                Map.of(
                        "event", "CALL_SERVICE",
                        "tableId", tableId,
                        "status", status.name()
                )
        );

        String message = status == ServiceStatus.CALL_STAFF 
                ? "Bàn " + tableId + " đang gọi nhân viên!" 
                : "Đã tiếp nhận yêu cầu!";

        return ResponseEntity.ok(Map.of("message", message));
    }

    // ==========================================
    // II. GIỎ HÀNG TẠM (ĐỒNG BỘ MULTI-DEVICE CÙNG BÀN)
    // ==========================================

    @GetMapping("/cart/{tableId}")
    public ResponseEntity<CartResponseDTO> getCartOverview(@PathVariable Long tableId) {
        return ResponseEntity.ok(cartService.getCartOverview(tableId));
    }

    @PostMapping("/cart/add")
    public ResponseEntity<Map<String, String>> addItemToCart(@Valid @RequestBody CartItemRequestDTO dto) {
        cartService.addItemToCart(dto.getTableId(), dto.getItemId(), dto.getQuantity(), dto.getNote());
        
        notifyCartUpdate(dto.getTableId());
        
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
            notifyCartUpdate(tableId);
            return ResponseEntity.ok(Map.of("message", "Đã xóa món khỏi giỏ hàng!"));
        }

        cartService.updateCartItemDetail(tableId, itemId, quantity, note);
        notifyCartUpdate(tableId);
        
        return ResponseEntity.ok(Map.of("message", "Cập nhật giỏ hàng thành công!"));
    }

    @DeleteMapping("/cart/items/{itemId}")
    public ResponseEntity<Map<String, String>> removeItemFromCart(
            @RequestParam Long tableId,
            @PathVariable Long itemId) {
            
        cartService.removeItemFromCart(tableId, itemId);
        notifyCartUpdate(tableId);
        
        return ResponseEntity.ok(Map.of("message", "Đã xóa món ăn khỏi giỏ hàng!"));
    }

    @DeleteMapping("/cart/clear")
    public ResponseEntity<Map<String, String>> clearCart(@RequestParam Long tableId) {
        cartService.clearTemporaryCart(tableId);
        notifyCartUpdate(tableId);
        
        return ResponseEntity.ok(Map.of("message", "Đã xóa toàn bộ món trong giỏ hàng tạm!"));
    }

    // ==========================================
    // III. BẤM GỌI MÓN (SUBMIT ORDER)
    // ==========================================

    @PostMapping("/confirm-order")
    public ResponseEntity<Map<String, String>> confirmOrder(@RequestParam Long tableId) {
        cartService.confirmOrder(tableId);

        // 1. Thông báo cho Nhân viên/Bếp biết có đơn mới cần duyệt/nấu
        messagingTemplate.convertAndSend(
                "/topic/staff/tables",
                Map.of(
                        "event", "NEW_ORDER",
                        "tableId", tableId,
                        "timestamp", System.currentTimeMillis()
                )
        );

        // 2. Đồng bộ giỏ hàng & danh sách đơn hàng cho Khách hàng tại bàn
        notifyCartUpdate(tableId);
        messagingTemplate.convertAndSend(
                "/topic/tables/" + tableId + "/orders",
                Map.of(
                        "event", "ORDER_PLACED",
                        "message", "Đơn hàng đã được gửi thành công!"
                )
        );

        return ResponseEntity.ok(Map.of("message", "Đã gửi đơn hàng thành công xuống bếp!"));
    }

    // ==========================================
    // IV. YÊU CẦU THANH TOÁN & HÓA ĐƠN
    // ==========================================

    @PostMapping("/payment/cash")
    public ResponseEntity<Map<String, String>> processCashPayment(@RequestParam Long tableId) {
        paymentService.processCashPayment(tableId);

        // Báo cho Nhân viên đến bàn thu tiền
        messagingTemplate.convertAndSend(
                "/topic/staff/tables",
                Map.of(
                        "event", "REQUEST_CASH_PAYMENT",
                        "tableId", tableId
                )
        );

        return ResponseEntity.ok(Map.of("message", "Đã gửi yêu cầu thanh toán tiền mặt. Vui lòng chờ nhân viên!"));
    }

    @GetMapping("/invoice-summary/{tableId}")
    public ResponseEntity<TableOrderSummaryDTO> getInvoiceSummary(@PathVariable Long tableId) {
        return ResponseEntity.ok(paymentService.getInvoiceSummaryDTO(tableId));
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    /**
     * Đồng bộ Giỏ hàng tạm giữa các thiết bị đang quét chung 1 mã QR bàn
     */
    private void notifyCartUpdate(Long tableId) {
        messagingTemplate.convertAndSend(
                "/topic/tables/" + tableId + "/cart",
                Map.of(
                        "event", "CART_UPDATED",
                        "tableId", tableId
                )
        );
    }
}