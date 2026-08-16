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
import org.springframework.web.bind.annotation.*;

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

        // Logic cập nhật DB và bắn WebSocket CALL_STAFF đã được xử lý trong orderService
        orderService.updateTableServiceStatus(tableId, status);

        String message = status == ServiceStatus.CALL_STAFF 
                ? "Bàn " + tableId + " đang gọi nhân viên!" 
                : "Đã tiếp nhận yêu cầu!";

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
            return ResponseEntity.ok(Map.of("message", "Đã xóa món khỏi giỏ hàng!"));
        }

        cartService.updateCartItemDetail(tableId, itemId, quantity, note);
        return ResponseEntity.ok(Map.of("message", "Cập nhật giỏ hàng thành công!"));
    }

    @DeleteMapping("/cart/items/{itemId}")
    public ResponseEntity<Map<String, String>> removeItemFromCart(
            @RequestParam Long tableId,
            @PathVariable Long itemId) {
            
        cartService.removeItemFromCart(tableId, itemId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa món ăn khỏi giỏ hàng!"));
    }

    @DeleteMapping("/cart/clear")
    public ResponseEntity<Map<String, String>> clearCart(@RequestParam Long tableId) {
        cartService.clearTemporaryCart(tableId);
        return ResponseEntity.ok(Map.of("message", "Đã xóa toàn bộ món trong giỏ hàng tạm!"));
    }

    // III. BẤM GỌI MÓN
    @PostMapping("/confirm-order")
    public ResponseEntity<Map<String, String>> confirmOrder(@RequestParam Long tableId) {
        cartService.confirmOrder(tableId);
        return ResponseEntity.ok(Map.of("message", "Đã gửi đơn hàng thành công xuống bếp!"));
    }

    // IV. YÊU CẦU THANH TOÁN & HÓA ĐƠN
    @PostMapping("/payment/cash")
    public ResponseEntity<Map<String, String>> processCashPayment(@RequestParam Long tableId) {
        paymentService.processCashPayment(tableId);
        return ResponseEntity.ok(Map.of("message", "Đã gửi yêu cầu thanh toán tiền mặt. Vui lòng chờ nhân viên!"));
    }

    @GetMapping("/invoice-summary/{tableId}")
    public ResponseEntity<TableOrderSummaryDTO> getInvoiceSummary(@PathVariable Long tableId) {
        return ResponseEntity.ok(paymentService.getInvoiceSummaryDTO(tableId));
    }
}