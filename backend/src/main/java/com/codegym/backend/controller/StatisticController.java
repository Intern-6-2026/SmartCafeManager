package com.codegym.backend.controller;

import com.codegym.backend.dto.DashboardStatsDTO;
import com.codegym.backend.dto.InvoiceResponseDTO;
import com.codegym.backend.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff/statistics")
@CrossOrigin("*")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    /**
     * 1. API Lấy danh sách hóa đơn (Màn hình 1)
     * URL: GET /api/v1/statistics/invoices
     * URL hỗ trợ lọc theo bàn: GET /api/v1/statistics/invoices?tableId=1
     */
    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoices(
            @RequestParam(required = false) Long tableId) {
        return ResponseEntity.ok(statisticService.getInvoices(tableId));
    }

    /**
     * 2. API Lấy dữ liệu Thống kê thu nhập (Màn hình 2 - Dashboard)
     * URL: GET /api/v1/statistics/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(statisticService.getDashboardStats());
    }
}