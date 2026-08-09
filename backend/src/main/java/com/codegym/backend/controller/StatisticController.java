package com.codegym.backend.controller;

import com.codegym.backend.dto.DashboardStatsDTO;
import com.codegym.backend.dto.InvoiceDetailResponseDTO;
import com.codegym.backend.dto.InvoiceResponseDTO;
import com.codegym.backend.service.OrderService;
import com.codegym.backend.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/staff/statistics")
@CrossOrigin("*")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;
    private final OrderService orderService; //Inject thêm OrderService

    /**
     * 1. Lấy danh sách hóa đơn (Tổng quan)
     */
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoices(
            @RequestParam(required = false) Long tableId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date
    ) {
        return ResponseEntity.ok(statisticService.getInvoices(tableId, type, date));
    }

    /**
     *. BỔ SUNG API: Lấy chi tiết món ăn của 1 hóa đơn khi click "Xem chi tiết"
     * GET /api/v1/staff/statistics/invoices/10
     */
    @GetMapping("/invoices/{orderId}")
    public ResponseEntity<InvoiceDetailResponseDTO> getInvoiceDetail(@PathVariable Long orderId) {
        // Truyền customerId = null vì nhân viên xem không cần check feedback
        return ResponseEntity.ok(orderService.getInvoiceDetailForCustomer(orderId, null));
    }

    /**
     * 3. Lấy dữ liệu Thống kê Dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(statisticService.getDashboardStats());
    }
}