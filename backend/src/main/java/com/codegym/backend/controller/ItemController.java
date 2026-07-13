package com.codegym.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codegym.backend.dto.ItemResponse;
import com.codegym.backend.service.ItemService;

@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    // Khớp với cấu hình Security: GET /api/v1/items
    @GetMapping("")
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    // Khớp với cấu hình Security: GET /api/v1/items/latest
    @GetMapping("/latest")
    public ResponseEntity<List<ItemResponse>> getLatestItems() {
        return ResponseEntity.ok(itemService.getLatestItems());
    }

    // Khớp với cấu hình Security: GET /api/v1/items/best-sellers
    @GetMapping("/best-sellers")
    public ResponseEntity<List<ItemResponse>> getBestSellerItems() {
        return ResponseEntity.ok(itemService.getBestSellerItems());
    }
}