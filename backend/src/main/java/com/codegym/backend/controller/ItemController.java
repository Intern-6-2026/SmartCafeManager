package com.codegym.backend.controller;

import com.codegym.backend.dto.ItemResponse;
import com.codegym.backend.service.ItemService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * Lấy toàn bộ danh sách món ăn đang hoạt động (chưa xóa)
     * API Link mới: http://localhost:8080/api/v1/items
     */
    @GetMapping
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    /**
     * Lấy danh sách các món ăn mới nhất
     * API Link mới: http://localhost:8080/api/v1/items/latest
     */
    @GetMapping("/latest")
    public ResponseEntity<List<ItemResponse>> getLatestItems() {
        return ResponseEntity.ok(itemService.getLatestItems());
    }

    /**
     * Lấy danh sách các món ăn bán chạy nhất
     * API Link mới: http://localhost:8080/api/v1/items/best-sellers
     */
    @GetMapping("/best-sellers")
    public ResponseEntity<List<ItemResponse>> getBestSellerItems() {
        return ResponseEntity.ok(itemService.getBestSellerItems());
    }
}