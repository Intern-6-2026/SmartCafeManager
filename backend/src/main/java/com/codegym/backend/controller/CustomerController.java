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

import com.codegym.backend.entity.OrderDetail;
import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.enums.PaymentMethod;
import com.codegym.backend.enums.StatusOrderDetail;
import com.codegym.backend.service.CustomerOrderService;

@RestController
@RequestMapping("/api/v1/customer")
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
    public ResponseEntity<List<OrderDetail>> getTemporaryCart(@RequestParam String tableName) {
        List<OrderDetail> cartItems = customerOrderService.getCartByStatus(tableName, StatusOrderDetail.PENDING);
        return ResponseEntity.ok(cartItems);
    }

    // 3. NGHIỆP VỤ: BẤM NÚT [GỌI MÓN] ĐỂ XÁC NHẬN GỬI XUỐNG BẾP (Chuyển PENDING -> CONFIRMED)
    @PostMapping("/confirm-order")
    public ResponseEntity<String> confirmOrder(@RequestParam String tableName) {
        customerOrderService.confirmOrder(tableName);
        return ResponseEntity.ok("Đã gửi đơn hàng thành công xuống bếp!");
    }

    // 4. NGHIỆP VỤ: XEM TẤT CẢ CÁC MÓN ĐÃ GỌI XUỐNG BẾP (Lấy món CONFIRMED hoặc SERVED)
    @GetMapping("/order-history")
    public ResponseEntity<List<OrderDetail>> getOrderHistory(@RequestParam String tableName) {
        List<OrderDetail> orderedItems = customerOrderService.getOrderedItems(tableName);
        return ResponseEntity.ok(orderedItems);
    }

    // 5. [BỊ THIẾU CŨ]: XEM CHI TIẾT HÓA ĐƠN LỚN (Để lấy tổng tiền totalAmount trước khi bấm thanh toán)
    @GetMapping("/invoice")
    public ResponseEntity<com.codegym.backend.dto.TableOrderSummaryDTO> getCurrentInvoice(@RequestParam String tableName) {
    return ResponseEntity.ok(customerOrderService.getInvoiceSummaryDTO(tableName));
    }
    // 6. [BỊ THIẾU CŨ]: KHÁCH ẤN NÚT YÊU CẦU THANH TOÁN (Chọn phương thức CASH, BANK_TRANSFER,...)
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
}