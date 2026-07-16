package com.codegym.backend.repository;

import com.codegym.backend.dto.ItemResponse;
import com.codegym.backend.entity.Item;
<<<<<<< HEAD
import com.codegym.backend.dto.ItemResponse; // Sử dụng DTO thay vì Projection
=======
>>>>>>> 6f367d9e48f4cbd30c14a9e766cc61f749f977ed
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

<<<<<<< HEAD
    List<Item> findByDeletedAtIsNullOrderByItemIdDesc(Pageable pageable);

    List<Item> findByDeletedAtIsNullOrderByTotalOrderCountDesc(Pageable pageable);

    // ĐÃ CẬP NHẬT: Thêm Pageable pageable để nhận tham số phân trang truyền vào từ Service
    @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
            + "FROM Item i LEFT JOIN i.category c "
            + "WHERE i.deletedAt IS NULL")
    List<ItemResponse> findAllItemsAsDTO(Pageable pageable);
=======
       List<Item> findByDeletedAtIsNullOrderByItemIdDesc(Pageable pageable);
>>>>>>> 6f367d9e48f4cbd30c14a9e766cc61f749f977ed

    @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
            + "FROM Item i JOIN i.category c "
            + "WHERE i.deletedAt IS NULL "
            + "ORDER BY i.createdAt DESC, i.itemId DESC")
    List<ItemResponse> findLatestItems(Pageable pageable);

<<<<<<< HEAD
    @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
            + "FROM Item i JOIN i.category c "
            + "WHERE i.deletedAt IS NULL "
            + "ORDER BY i.totalOrderCount DESC")
    List<ItemResponse> findBestSellerItems(Pageable pageable);
=======
       @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
                     +
                     "FROM Item i LEFT JOIN i.category c "
                     +
                     "WHERE i.deletedAt IS NULL")
       List<ItemResponse> findAllItemsAsDTO();

       @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
                     +
                     "FROM Item i JOIN i.category c "
                     +
                     "WHERE i.deletedAt IS NULL "
                     +
                     "ORDER BY i.createdAt DESC, i.itemId DESC")
       List<ItemResponse> findLatestItems(Pageable pageable);

       @Query("SELECT new com.codegym.backend.dto.ItemResponse(i.itemId, i.itemCode, c.categoryId, c.categoryName, i.itemName, i.price, i.description, i.imageUrl, i.isAvailable, i.totalOrderCount) "
                     +
                     "FROM Item i JOIN i.category c "
                     +
                     "WHERE i.deletedAt IS NULL "
                     +
                     "ORDER BY i.totalOrderCount DESC")
       List<ItemResponse> findBestSellerItems(Pageable pageable);
>>>>>>> 6f367d9e48f4cbd30c14a9e766cc61f749f977ed
}