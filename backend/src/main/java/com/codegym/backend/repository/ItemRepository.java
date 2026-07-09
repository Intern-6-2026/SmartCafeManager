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

       // 1. Tìm các món sắp xếp theo ID giảm dần (mới nhất) kèm giới hạn số lượng
       List<Item> findByOrderByItemIdDesc(Pageable pageable);

       // 2. Tìm các món sắp xếp theo số lượng bán giảm dần (bán chạy nhất) kèm giới
       // hạn số lượng
       List<Item> findByOrderByTotalOrderCountDesc(Pageable pageable);

       @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
                     +
                     "FROM Item i JOIN i.category c")
       List<ItemResponse> findAllItemsAsDTO();

       @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
                     +
                     "FROM Item i JOIN i.category c ORDER BY i.createdAt DESC, i.itemId DESC")
       List<ItemResponse> findLatestItems(Pageable pageable);

       @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
                     +
                     "FROM Item i JOIN i.category c ORDER BY i.totalOrderCount DESC")
       List<ItemResponse> findBestSellerItems(Pageable pageable);
}