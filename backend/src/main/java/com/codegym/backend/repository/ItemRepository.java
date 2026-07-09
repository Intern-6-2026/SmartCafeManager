package com.codegym.backend.repository;

import com.codegym.backend.dto.ItemResponse;
import com.codegym.backend.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

       // 1. Tìm các món ăn mới nhất kèm giới hạn số lượng và chưa bị xóa mềm
       List<Item> findByDeletedAtIsNullOrderByItemIdDesc(Pageable pageable);

       // 2. Tìm các món ăn bán chạy nhất kèm giới hạn số lượng và chưa bị xóa mềm
       List<Item> findByDeletedAtIsNullOrderByTotalOrderCountDesc(Pageable pageable);

       // 3. Lấy tất cả DTO món ăn đang hoạt động trong hệ thống
       @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
                     +
                     "FROM Item i JOIN i.category c "
                     +
                     "WHERE i.deletedAt IS NULL")
       List<ItemResponse> findAllItemsAsDTO();

       // 4. Lấy DTO các món ăn mới nhất và chưa bị xóa mềm
       @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
                     +
                     "FROM Item i JOIN i.category c "
                     +
                     "WHERE i.deletedAt IS NULL "
                     +
                     "ORDER BY i.createdAt DESC, i.itemId DESC")
       List<ItemResponse> findLatestItems(Pageable pageable);

       // 5. Lấy DTO các món ăn bán chạy nhất và chưa bị xóa mềm
       @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
                     +
                     "FROM Item i JOIN i.category c "
                     +
                     "WHERE i.deletedAt IS NULL "
                     +
                     "ORDER BY i.totalOrderCount DESC")
       List<ItemResponse> findBestSellerItems(Pageable pageable);
}