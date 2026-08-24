package com.codegym.backend.controller;

import com.codegym.backend.dto.AdminTableOrderRequestDTO;
import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.service.AdminTableOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/orders")
@CrossOrigin("*")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTableOrderController {

    private final AdminTableOrderService adminTableOrderService;

    @GetMapping
    public ResponseEntity<List<TableOrder>> getAllOrders() {
        return ResponseEntity.ok(adminTableOrderService.getAllOrders());
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<TableOrder> updateOrder(
            @PathVariable Long orderId,
            @RequestBody AdminTableOrderRequestDTO dto) {
        return ResponseEntity.ok(adminTableOrderService.updateOrder(orderId, dto));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Map<String, String>> deleteOrder(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "Admin thao tác hủy đơn") String reason) {
        adminTableOrderService.softDeleteOrder(orderId, reason);
        return ResponseEntity.ok(Map.of(
                "message", "Đã hủy và xóa mềm hóa đơn thành công!",
                "orderId", String.valueOf(orderId)
        ));
    }
}