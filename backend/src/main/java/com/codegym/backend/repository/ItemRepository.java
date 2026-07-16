package com.codegym.backend.repository;

import com.codegym.backend.entity.Item;
import com.codegym.backend.dto.ItemResponse; // Sử dụng DTO thay vì Projection
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByDeletedAtIsNullOrderByItemIdDesc(Pageable pageable);

    List<Item> findByDeletedAtIsNullOrderByTotalOrderCountDesc(Pageable pageable);

    // ĐÃ CẬP NHẬT: Thêm Pageable pageable để nhận tham số phân trang truyền vào từ Service
    @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
            + "FROM Item i LEFT JOIN i.category c "
            + "WHERE i.deletedAt IS NULL")
    List<ItemResponse> findAllItemsAsDTO(Pageable pageable);

    @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
            + "FROM Item i JOIN i.category c "
            + "WHERE i.deletedAt IS NULL "
            + "ORDER BY i.createdAt DESC, i.itemId DESC")
    List<ItemResponse> findLatestItems(Pageable pageable);

    @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
            + "FROM Item i JOIN i.category c "
            + "WHERE i.deletedAt IS NULL "
            + "ORDER BY i.totalOrderCount DESC")
    List<ItemResponse> findBestSellerItems(Pageable pageable);
}