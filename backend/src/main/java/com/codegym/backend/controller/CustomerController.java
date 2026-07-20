package com.codegym.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codegym.backend.dto.CartItemResponse;
import com.codegym.backend.dto.TableOrderSummaryDTO;
import com.codegym.backend.entity.Tables;
import com.codegym.backend.enums.PaymentMethod;
import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.enums.StatusOrderDetail;
import com.codegym.backend.service.CustomerOrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/items")
@CrossOrigin("*")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerOrderService customerOrderService;

    // 1. THÊM MÓN VÀO GIỎ TẠM THỜI (PENDING)
    @PostMapping("/add-item")
    public ResponseEntity<String> addItemToCart(
            @RequestParam String tableName,
            @RequestParam Long itemId,
            @RequestParam Integer quantity,
            @RequestParam(required = false, defaultValue = "") String note) {
        customerOrderService.addItemToCart(tableName, itemId, quantity, note);
        return ResponseEntity.ok("Đã thêm món vào giỏ hàng tạm thời!");
    }

    // 2. XEM TẤT CẢ MÓN TRONG GIỎ HÀNG TẠM (Chỉ lấy món PENDING)
    @GetMapping("/cart")
    public ResponseEntity<List<CartItemResponse>> getTemporaryCart(@RequestParam String tableName) {
        List<CartItemResponse> cartItems = customerOrderService.getCartByStatus(tableName, StatusOrderDetail.PENDING);
        return ResponseEntity.ok(cartItems);
    }

    // 3. BẤM NÚT [GỌI MÓN] ĐỂ XÁC NHẬN GỬI XUỐNG BẾP (Chuyển PENDING -> CONFIRMED)
    @PostMapping("/confirm-order")
    public ResponseEntity<String> confirmOrder(@RequestParam String tableName) {
        customerOrderService.confirmOrder(tableName);
        return ResponseEntity.ok("Đã gửi đơn hàng thành công xuống bếp!");
    }

    // 4. XEM TẤT CẢ CÁC MÓN ĐÃ GỌI XUỐNG BẾP
    @GetMapping("/order-history")
    public ResponseEntity<List<CartItemResponse>> getOrderHistory(@RequestParam String tableName) {
        List<CartItemResponse> orderedItems = customerOrderService.getOrderedItems(tableName);
        return ResponseEntity.ok(orderedItems);
    }

    // 5. XEM CHI TIẾT HÓA ĐƠN LỚN (Lấy tổng tiền & danh sách món dạng DTO)
    @GetMapping("/invoice")
    public ResponseEntity<TableOrderSummaryDTO> getCurrentInvoice(@RequestParam String tableName) {
        return ResponseEntity.ok(customerOrderService.getInvoiceSummaryDTO(tableName));
    }

    // 6. KHÁCH ẤN NÚT YÊU CẦU THANH TOÁN
    @PostMapping("/request-checkout")
    public ResponseEntity<String> requestCheckout(
            @RequestParam String tableName,
            @RequestParam PaymentMethod paymentMethod) {
        customerOrderService.requestCheckout(tableName, paymentMethod);
        return ResponseEntity.ok("Yêu cầu thanh toán bằng " + paymentMethod + " đã được gửi! Nhân viên sẽ đến ngay.");
    }

    // 7. YÊU CẦU DỊCH VỤ KHÁC (Gọi nhân viên, đổi trạng thái bàn)
    @PostMapping("/call-service")
    public ResponseEntity<String> callService(
            @RequestParam String tableName,
            @RequestParam ServiceStatus status) {
        customerOrderService.updateTableServiceStatus(tableName, status);
        return ResponseEntity.ok("Hệ thống đã ghi nhận yêu cầu: " + status);
    }

    // 8. SỬA SỐ LƯỢNG SẢN PHẨM TRONG GIỎ HÀNG TẠM
    @PostMapping("/update-quantity")
    public ResponseEntity<String> updateCartItemQuantity(
            @RequestParam String tableName,
            @RequestParam Long itemId,
            @RequestParam Integer newQuantity) {

        if (newQuantity <= 0) {
            customerOrderService.removeItemFromCart(tableName, itemId);
            return ResponseEntity.ok("Số lượng nhỏ hơn hoặc bằng 0. Đã xóa món khỏi giỏ hàng!");
        }

        customerOrderService.updateItemQuantityInCart(tableName, itemId, newQuantity);
        return ResponseEntity.ok("Đã cập nhật số lượng món ăn!");
    }

    // 9. XÓA MỘT SẢN PHẨM KHỎI GIỎ HÀNG TẠM
    @PostMapping("/remove-item")
    public ResponseEntity<String> removeItemFromCart(
            @RequestParam String tableName,
            @RequestParam Long itemId) {
        customerOrderService.removeItemFromCart(tableName, itemId);
        return ResponseEntity.ok("Đã xóa món ăn khỏi giỏ hàng tạm thời!");
    }

    // 10. XÓA SẠCH GIỎ HÀNG TẠM
    @PostMapping("/clear-cart")
    public ResponseEntity<String> clearCart(@RequestParam String tableName) {
        customerOrderService.clearTemporaryCart(tableName);
        return ResponseEntity.ok("Đã xóa toàn bộ món trong giỏ hàng tạm!");
    }

    // 11. BỔ SUNG: LẤY THÔNG TIN CHI TIẾT BÀN
    @GetMapping("/table-info")
    public ResponseEntity<Tables> getTableInfo(@RequestParam String tableName) {
        return ResponseEntity.ok(customerOrderService.getTableInfo(tableName));
    }

    // 12. BỔ SUNG: BẾP ĐÁNH DẤU MÓN ĐÃ CHẾ BIẾN XONG (CONFIRMED -> SERVED)
    @PutMapping("/kitchen/serve-item")
    public ResponseEntity<String> markItemAsServed(@RequestParam Long orderDetailId) {
        customerOrderService.markItemAsServed(orderDetailId);
        return ResponseEntity.ok("Đã chuyển trạng thái món sang SERVED (Đã phục vụ)!");
    }

    // 13. BỔ SUNG: BẾP HỦY MÓN DO HẾT HÀNG (SẼ TỰ ĐỘNG CẬP NHẬT LẠI TỔNG TIỀN)
    @PutMapping("/kitchen/cancel-item")
    public ResponseEntity<String> cancelOrderItem(
            @RequestParam Long orderDetailId,
            @RequestParam(required = false, defaultValue = "Hết món") String reason) {
        customerOrderService.cancelOrderItem(orderDetailId, reason);
        return ResponseEntity.ok("Đã hủy món thành công và tính lại tổng tiền hóa đơn!");
    }
    // 14. HOÀN TẤT THANH TOÁN HÓA ĐƠN (Chuyển trạng thái hóa đơn -> PAID, Bàn -> EMPTY/AVAILABLE)
    @PostMapping("/complete-checkout")
    public ResponseEntity<String> completeCheckout(
        @RequestParam String tableName,
        @RequestParam PaymentMethod paymentMethod) {
    
    customerOrderService.completeCheckout(tableName, paymentMethod);
    return ResponseEntity.ok("Thanh toán thành công cho " + tableName + "! Hóa đơn đã chốt và bàn đã giải phóng.");
}
}