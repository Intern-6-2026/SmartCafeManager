package com.codegym.backend.controller;

import java.math.BigDecimal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.codegym.backend.dto.ItemResponse;
import com.codegym.backend.service.ItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/items")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AdminItemController {

    private final ItemService itemService;

    // 1. LẤY CHI TIẾT MỘT MÓN ĂN THEO ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        try {
            ItemResponse item = itemService.getItemById(id);
            return ResponseEntity.ok(item);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Không tìm thấy món ăn: " + e.getMessage());
        }
    }

    // 2. THÊM MỚI MÓN ĂN
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createItem(
            @RequestParam("itemCode") String itemCode,
            @RequestParam("itemName") String itemName,
            @RequestParam("price") BigDecimal price,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "categoryId", required = false) Long categoryId, 
            @RequestParam(value = "newCategoryName", required = false) String newCategoryName, 
            @RequestPart(value = "image", required = false) MultipartFile image 
    ) {
        try {
            ItemResponse newItem = itemService.createItem(
                    itemCode, itemName, price, description, categoryId, newCategoryName, image
            );
            return ResponseEntity.ok(newItem);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi thêm món mới: " + e.getMessage());
        }
    }

    // 3. CẬP NHẬT (SỬA) MÓN ĂN - ĐÃ LÀM TƯƠNG TỰ (RÃ PHẲNG THAM SỐ, CÓ CHỌN FILE ẢNH MỚI)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateItem(
            @PathVariable Long id,
            @RequestParam("itemCode") String itemCode,
            @RequestParam("itemName") String itemName,
            @RequestParam("price") BigDecimal price,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "newCategoryName", required = false) String newCategoryName,
            @RequestParam(value = "isAvailable", required = false, defaultValue = "true") Boolean isAvailable,
            @RequestPart(value = "image", required = false) MultipartFile image // Cho phép tải ảnh mới lên để thay thế ảnh cũ
    ) {
        try {
            ItemResponse updatedItem = itemService.updateItem(
                    id, itemCode, itemName, price, description, categoryId, newCategoryName, isAvailable, image
            );
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi cập nhật món ăn: " + e.getMessage());
        }
    }

    // 4. XÓA MÓN ĂN (Ẩn món - Xóa mềm)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable Long id) {
        try {
            itemService.deleteItem(id);
            return ResponseEntity.ok("Đã ngưng phục vụ món ăn thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi xóa món ăn: " + e.getMessage());
        }
    }
}