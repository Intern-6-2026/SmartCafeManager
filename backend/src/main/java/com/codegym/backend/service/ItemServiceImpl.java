package com.codegym.backend.service;

import java.io.IOException; // Xử lý lỗi upload file
import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.codegym.backend.dto.ItemResponse;
import com.codegym.backend.entity.Item;
import com.codegym.backend.entity.MenuCategory;
import com.codegym.backend.repository.ItemRepository;
import com.codegym.backend.repository.MenuCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final CloudinaryService cloudinaryService; // Inject CloudinaryService thật

    // ==========================================
    // --- 1. LẤY DANH SÁCH MÓN ĂN ---
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        // Sử dụng PageRequest.of(0, Integer.MAX_VALUE) nếu hàm Repository yêu cầu Pageable,
        // hoặc gọi thẳng hàm lấy tất cả đã đồng bộ.
        return itemRepository.findAllItemsAsDTO(PageRequest.of(0, Integer.MAX_VALUE));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getLatestItems() {
        return itemRepository.findLatestItems(PageRequest.of(0, 4));
    }

    @Override
    @Transactional(readOnly = true)
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
    public ItemResponse createItem(String itemCode, String itemName, BigDecimal price, String description, Long menuCategoryId, String newMenuCategoryName, MultipartFile image) {
        String imageUrl = null;
        
        // Sử dụng CloudinaryService thật để lấy URL ảnh từ file upload
        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = cloudinaryService.uploadImage(image);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi upload ảnh lên Cloudinary: " + e.getMessage());
            }
        }

        // Logic xử lý tìm kiếm hoặc tự động tạo mới MenuCategory
        MenuCategory menuCategory = null;
        if (newMenuCategoryName != null && !newMenuCategoryName.trim().isEmpty()) {
            menuCategory = menuCategoryRepository.findByCategoryName(newMenuCategoryName.trim())
                    .orElseGet(() -> {
                        MenuCategory newCat = new MenuCategory();
                        newCat.setCategoryName(newMenuCategoryName.trim()); 
                        return menuCategoryRepository.save(newCat);
                    });
        } else if (menuCategoryId != null) {
            menuCategory = menuCategoryRepository.findById(menuCategoryId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + menuCategoryId));
        }

        Item item = Item.builder()
                .itemCode(itemCode)
                .itemName(itemName)
                .price(price)
                .description(description)
                .imageUrl(imageUrl)
                .isAvailable(true)
                .totalOrderCount(0)
                .category(menuCategory) 
                .build();

        Item savedItem = itemRepository.save(item);
        return mapToItemResponse(savedItem);
    }

    // ==========================================
    // --- 3. CẬP NHẬT MÓN ĂN ---
    // ==========================================
    @Override
    @Transactional
    public ItemResponse updateItem(Long itemId, String itemCode, String itemName, BigDecimal price, String description, Long menuCategoryId, String newMenuCategoryName, Boolean isAvailable, MultipartFile image) {
        // 1. Lấy món ăn cũ từ DB ra làm gốc
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn với ID: " + itemId));

        // 2. Chỉ cập nhật những trường thực sự được truyền lên (Tránh bị đè null)
        if (itemCode != null && !itemCode.trim().isEmpty()) {
            existingItem.setItemCode(itemCode);
        }
        if (itemName != null && !itemName.trim().isEmpty()) {
            existingItem.setItemName(itemName);
        }
        if (price != null) {
            existingItem.setPrice(price);
        }
        if (description != null) {
            existingItem.setDescription(description);
        }
        if (isAvailable != null) {
            existingItem.setIsAvailable(isAvailable);
        }

        // 3. Xử lý ảnh: Upload ảnh thật lên Cloudinary nếu có file mới gửi lên
        if (image != null && !image.isEmpty()) {
            try {
                String newImageUrl = cloudinaryService.uploadImage(image);
                existingItem.setImageUrl(newImageUrl); 
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi upload ảnh mới lên Cloudinary: " + e.getMessage());
            }
        }

        // 4. Tìm hoặc tạo mới MenuCategory lúc sửa đổi thông tin
        if (newMenuCategoryName != null && !newMenuCategoryName.trim().isEmpty()) {
            MenuCategory menuCategory = menuCategoryRepository.findByCategoryName(newMenuCategoryName.trim())
                    .orElseGet(() -> {
                        MenuCategory newCat = new MenuCategory();
                        newCat.setCategoryName(newMenuCategoryName.trim());
                        return menuCategoryRepository.save(newCat);
                    });
            existingItem.setCategory(menuCategory);
        } else if (menuCategoryId != null) {
            MenuCategory menuCategory = menuCategoryRepository.findById(menuCategoryId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
            existingItem.setCategory(menuCategory);
        }

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
        existingItem.setIsAvailable(false); // Xóa mềm: đánh dấu không còn hoạt động
        itemRepository.save(existingItem);
    }

    // ==========================================
    // --- 5. HÀM MAPPING SẠCH (Item -> ItemResponse) ---
    // ==========================================
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