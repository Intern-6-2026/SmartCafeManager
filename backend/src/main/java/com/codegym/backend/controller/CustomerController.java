package com.codegym.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codegym.backend.dto.CartItemResponse;
import com.codegym.backend.enums.PaymentMethod;
import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.enums.StatusOrderDetail;
import com.codegym.backend.service.CustomerOrderService;

@RestController
@RequestMapping("/api/v1/items")
@CrossOrigin("*")
public class CustomerController {

    @Autowired
    private CustomerOrderService customerOrderService;

    // 1. NGHIỆP VỤ: THÊM MÓN VÀO GIỎ TẠM THỜI
    @PostMapping("/add-item")
    public ResponseEntity<String> addItemToCart(
            @RequestParam String tableName,
            @RequestParam Long itemId,
            @RequestParam Integer quantity,
            @RequestParam(required = false, defaultValue = "") String note) {
        customerOrderService.addItemToCart(tableName, itemId, quantity, note);
        return ResponseEntity.ok("Đã thêm món vào giỏ hàng tạm thời!");
    }

// 2. NGHIỆP VỤ: XEM TẤT CẢ MÓN TRONG GIỎ HÀNG TẠM (Chỉ lấy món PENDING)
    @GetMapping("/cart")
    // Đảm bảo kiểu trả về trong ResponseEntity là List<CartItemResponse>
    public ResponseEntity<List<CartItemResponse>> getTemporaryCart(@RequestParam String tableName) {
    List<CartItemResponse> cartItems = customerOrderService.getCartByStatus(tableName, StatusOrderDetail.PENDING);
    return ResponseEntity.ok(cartItems);
    }
    // 3. NGHIỆP VỤ: BẤM NÚT [GỌI MÓN] ĐỂ XÁC NHẬN GỬI XUỐNG BẾP (Chuyển PENDING -> CONFIRMED)
    @PostMapping("/confirm-order")
    public ResponseEntity<String> confirmOrder(@RequestParam String tableName) {
        customerOrderService.confirmOrder(tableName);
        return ResponseEntity.ok("Đã gửi đơn hàng thành công xuống bếp!");
    }

    // 4. ĐÃ SỬA: XEM TẤT CẢ CÁC MÓN ĐÃ GỌI XUỐNG BẾP (Chuyển sang dùng CartItemResponse DTO sạch)
    @GetMapping("/order-history")
    public ResponseEntity<List<CartItemResponse>> getOrderHistory(@RequestParam String tableName) {
        List<CartItemResponse> orderedItems = customerOrderService.getOrderedItems(tableName);
        return ResponseEntity.ok(orderedItems);
    }

    // 5. NGHIỆP VỤ: XEM CHI TIẾT HÓA ĐƠN LỚN (Lấy tổng tiền totalAmount trước khi bấm thanh toán)
    @GetMapping("/invoice")
    public ResponseEntity<com.codegym.backend.dto.TableOrderSummaryDTO> getCurrentInvoice(@RequestParam String tableName) {
        return ResponseEntity.ok(customerOrderService.getInvoiceSummaryDTO(tableName));
    }

    // 6. NGHIỆP VỤ: KHÁCH ẤN NÚT YÊU CẦU THANH TOÁN (Chọn phương thức CASH, BANK_TRANSFER,...)
    @PostMapping("/request-checkout")
    public ResponseEntity<String> requestCheckout(
            @RequestParam String tableName,
            @RequestParam PaymentMethod paymentMethod) {
        customerOrderService.requestCheckout(tableName, paymentMethod);
        return ResponseEntity.ok("Yêu cầu thanh toán bằng " + paymentMethod + " đã được gửi! Nhân viên sẽ đến ngay.");
    }

    // 7. NGHIỆP VỤ: CÁC YÊU CẦU DỊCH VỤ KHÁC (Gọi nhân viên, báo sự cố...)
    @PostMapping("/call-service")
    public ResponseEntity<String> callService(
            @RequestParam String tableName,
            @RequestParam ServiceStatus status) {
        customerOrderService.updateTableServiceStatus(tableName, status);
        return ResponseEntity.ok("Hệ thống đã ghi nhận yêu cầu: " + status);
    }

    // 8. NGHIỆP VỤ: SỬA SỐ LƯỢNG SẢN PHẨM TRONG GIỎ HÀNG TẠM
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

    // 9. NGHIỆP VỤ: XÓA MỘT SẢN PHẨM KHỎI GIỎ HÀNG TẠM
    @PostMapping("/remove-item")
    public ResponseEntity<String> removeItemFromCart(
            @RequestParam String tableName,
            @RequestParam Long itemId) {
        customerOrderService.removeItemFromCart(tableName, itemId);
        return ResponseEntity.ok("Đã xóa món ăn khỏi giỏ hàng tạm thời!");
    }

    // 10. NGHIỆP VỤ: XÓA SẠCH GIỎ HÀNG TẠM (Hủy giỏ hàng)
    @PostMapping("/clear-cart")
    public ResponseEntity<String> clearCart(@RequestParam String tableName) {
        customerOrderService.clearTemporaryCart(tableName);
        return ResponseEntity.ok("Đã xóa toàn bộ món trong giỏ hàng tạm!");
    }
}