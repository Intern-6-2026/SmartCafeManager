package com.codegym.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.codegym.backend.dto.ItemResponse;
import com.codegym.backend.entity.Item;
import com.codegym.backend.repository.ItemRepository;
// import com.codegym.backend.repository.CategoryRepository; // Bật dòng này nếu anh có Repo Category

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    // private final CategoryRepository categoryRepository; // Bật dòng này nếu anh có Repo Category

    @Override
    public List<ItemResponse> getAllItems() {
        return itemRepository.findAllItemsAsDTO();
    }

    @Override
    public List<ItemResponse> getLatestItems() {
        return itemRepository.findLatestItems(PageRequest.of(0, 4));
    }

    @Override
    public List<ItemResponse> getBestSellerItems() {
        return itemRepository.findBestSellerItems(PageRequest.of(0, 4));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn với ID: " + itemId));
        return mapToItemResponse(item);
    }

    // ==========================================
    // --- 2. THÊM MỚI MÓN ĂN ---
    // ==========================================
    @Override
    @Transactional
    public ItemResponse createItem(String itemCode, String itemName, BigDecimal price, String description, Long categoryId, String newCategoryName, MultipartFile image) {
        String imageUrl = null;
        
        // Upload ảnh lên Cloudinary nếu có file
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = "https://res.cloudinary.com/demo/image/upload/v1572212344/sample.jpg"; 
                // Khi chạy thật: imageUrl = cloudinaryService.uploadImage(image);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi upload ảnh: " + e.getMessage());
            }
        }

        // Logic Category
        // com.codegym.backend.entity.Category category = null;
        /* Bật đoạn này khi đã sẵn sàng kết nối CategoryRepository
        if (newCategoryName != null && !newCategoryName.trim().isEmpty()) {
            category = categoryRepository.findByCategoryName(newCategoryName.trim())
                    .orElseGet(() -> {
                        com.codegym.backend.entity.Category newCat = new com.codegym.backend.entity.Category();
                        newCat.setCategoryName(newCategoryName.trim());
                        return categoryRepository.save(newCat);
                    });
        } else if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
        }
        */

        Item item = Item.builder()
                .itemCode(itemCode)
                .itemName(itemName)
                .price(price)
                .description(description)
                .imageUrl(imageUrl)
                .isAvailable(true)
                .totalOrderCount(0)
                // .category(category)
                .build();

        Item savedItem = itemRepository.save(item);
        return mapToItemResponse(savedItem);
    }

    // ==========================================
    // --- 3. CẬP NHẬT (SỬA) MÓN ĂN ---
    // ==========================================
    @Override
    @Transactional
    public ItemResponse updateItem(Long itemId, String itemCode, String itemName, BigDecimal price, String description, Long categoryId, String newCategoryName, Boolean isAvailable, MultipartFile image) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn với ID: " + itemId));

        // Cập nhật thông tin cơ bản
        existingItem.setItemCode(itemCode);
        existingItem.setItemName(itemName);
        existingItem.setPrice(price);
        existingItem.setDescription(description);
        existingItem.setIsAvailable(isAvailable != null ? isAvailable : existingItem.getIsAvailable());

        // Xử lý upload ảnh mới (Nếu người dùng không chọn ảnh mới, giữ nguyên ảnh cũ)
        if (image != null && !image.isEmpty()) {
            try {
                String newImageUrl = "https://res.cloudinary.com/demo/image/upload/v1572212344/sample.jpg"; 
                // Khi chạy thật: newImageUrl = cloudinaryService.uploadImage(image);
                existingItem.setImageUrl(newImageUrl); 
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi upload ảnh mới lên Cloudinary: " + e.getMessage());
            }
        }

        // Logic xử lý Category lúc sửa
        // com.codegym.backend.entity.Category category = null;
        /* Bật đoạn này khi đã sẵn sàng kết nối CategoryRepository
        if (newCategoryName != null && !newCategoryName.trim().isEmpty()) {
            category = categoryRepository.findByCategoryName(newCategoryName.trim())
                    .orElseGet(() -> {
                        com.codegym.backend.entity.Category newCat = new com.codegym.backend.entity.Category();
                        newCat.setCategoryName(newCategoryName.trim());
                        return categoryRepository.save(newCat);
                    });
            existingItem.setCategory(category);
        } else if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
            existingItem.setCategory(category);
        }
        */

        Item updatedItem = itemRepository.save(existingItem);
        return mapToItemResponse(updatedItem);
    }

    // ==========================================
    // --- 4. XÓA MÓN ĂN ---
    // ==========================================
    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn với ID: " + itemId));
        existingItem.setIsAvailable(false);
        itemRepository.save(existingItem);
    }

    private ItemResponse mapToItemResponse(Item item) {
        return ItemResponse.builder()
                .itemId(item.getItemId())
                .itemCode(item.getItemCode())
                .categoryId(item.getCategory() != null ? item.getCategory().getCategoryId() : null)
                .categoryName(item.getCategory() != null ? item.getCategory().getCategoryName() : null)
                .itemName(item.getItemName())
                .price(item.getPrice())
                .description(item.getDescription())
                .imageUrl(item.getImageUrl())
                .isAvailable(item.getIsAvailable())
                .totalOrderCount(item.getTotalOrderCount())
                .build();
    }
}