package com.codegym.backend.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.codegym.backend.dto.ItemResponse;

public interface ItemService {
    List<ItemResponse> getAllItems();
    List<ItemResponse> getLatestItems();
    List<ItemResponse> getBestSellerItems();
    ItemResponse getItemById(Long itemId);
    
    // Hàm Thêm mới phẳng
    ItemResponse createItem(String itemCode, String itemName, BigDecimal price, String description, Long categoryId, String newCategoryName, MultipartFile image);
    
    // Hàm Sửa phẳng tương tự Thêm mới
    ItemResponse updateItem(Long itemId, String itemCode, String itemName, BigDecimal price, String description, Long categoryId, String newCategoryName, Boolean isAvailable, MultipartFile image);
    
    void deleteItem(Long itemId);
}