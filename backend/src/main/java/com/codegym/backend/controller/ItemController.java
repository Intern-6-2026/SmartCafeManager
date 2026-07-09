package com.codegym.backend.controller;

import com.codegym.backend.entity.Item;
import com.codegym.backend.service.ItemService;
import com.codegym.backend.dto.ItemResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class ItemController {

    @Autowired
    private ItemService itemService;

    // Link 1: http://localhost:8080/api/v1/auth/items
    @GetMapping("/api/v1/auth/items")
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    // Link 2: http://localhost:8080/api/v1/auth/items/latest
    @GetMapping("/api/v1/auth/items/latest")
    public ResponseEntity<List<ItemResponse>> getLatestItems() {
        return ResponseEntity.ok(itemService.getLatestItems());
    }

    // Link 3: http://localhost:8080/api/v1/auth/items/best-sellers
    @GetMapping("/api/v1/auth/items/best-sellers")
    public ResponseEntity<List<ItemResponse>> getBestSellerItems() {
        return ResponseEntity.ok(itemService.getBestSellerItems());
    }
}