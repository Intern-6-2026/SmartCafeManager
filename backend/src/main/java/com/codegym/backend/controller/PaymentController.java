package com.codegym.backend.controller;

import com.codegym.backend.dto.TableOrderSummaryDTO;
import com.codegym.backend.enums.PaymentMethod;
import com.codegym.backend.service.CustomerOrderService;
import com.codegym.backend.service.PayPalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/items/payment")
@CrossOrigin("*")
@RequiredArgsConstructor
public class PaymentController {

    private final CustomerOrderService customerOrderService;
    private final PayPalService payPalService;

    // 1. API tạo thanh toán PayPal (Sửa String tableName -> Long tableId)
    @PostMapping("/paypal")
    public ResponseEntity<?> createPayPalPayment(@RequestParam Long tableId) {
        try {
            TableOrderSummaryDTO invoice = customerOrderService.getInvoiceSummaryDTO(tableId);

            String returnUrl = "http://localhost:3000/payment-success?tableId=" + tableId;
            String cancelUrl = "http://localhost:3000/payment-cancel?tableId=" + tableId;

            String approvalUrl = payPalService.createPayPalOrder(invoice.getTotalAmount(), returnUrl, cancelUrl);
            String qrCodeImageUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + approvalUrl;

            Map<String, String> response = new HashMap<>();
            response.put("approvalUrl", approvalUrl);
            response.put("qrCodeUrl", qrCodeImageUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tạo thanh toán PayPal: " + e.getMessage());
        }
    }

    // 2. API Callback sau khi khách bấm thanh toán xong ở PayPal (Sửa String tableName -> Long tableId)
    @GetMapping("/paypal/success")
    public ResponseEntity<?> paymentSuccess(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId,
            @RequestParam("tableId") Long tableId) {
        try {
            boolean isExecuted = payPalService.executePayment(paymentId, payerId);
            if (isExecuted) {
                // TỰ ĐỘNG ĐÓNG HÓA ĐƠN & RESET BÀN VỀ TRỐNG
                customerOrderService.completeCheckout(tableId, PaymentMethod.E_WALLET);
                return ResponseEntity.ok("Thanh toán thành công! Bàn đã được dọn sạch.");
            }
            return ResponseEntity.badRequest().body("Thanh toán thất bại!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xử lý thanh toán: " + e.getMessage());
        }
    }
}