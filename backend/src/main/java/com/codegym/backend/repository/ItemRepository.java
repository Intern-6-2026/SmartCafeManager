package com.codegym.backend.repository;

import com.codegym.backend.entity.Item;
import com.codegym.backend.dto.ItemProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i.itemId as itemId, i.itemCode as itemCode, c.categoryId as categoryId, " +
           "c.categoryName as categoryName, i.itemName as itemName, i.price as price, " +
           "i.description as description, i.imageUrl as imageUrl, i.isAvailable as isAvailable, " +
           "i.totalOrderCount as totalOrderCount " +
           "FROM Item i JOIN i.category c")
    List<ItemProjection> findAllItemsAsDTO(Pageable pageable);

    @Query("SELECT i.itemId as itemId, i.itemCode as itemCode, c.categoryId as categoryId, " +
           "c.categoryName as categoryName, i.itemName as itemName, i.price as price, " +
           "i.description as description, i.imageUrl as imageUrl, i.isAvailable as isAvailable, " +
           "i.totalOrderCount as totalOrderCount " +
           "FROM Item i JOIN i.category c ORDER BY i.createdAt DESC, i.itemId DESC")
    List<ItemProjection> findLatestItems(Pageable pageable);

    @Query("SELECT i.itemId as itemId, i.itemCode as itemCode, c.categoryId as categoryId, " +
           "c.categoryName as categoryName, i.itemName as itemName, i.price as price, " +
           "i.description as description, i.imageUrl as imageUrl, i.isAvailable as isAvailable, " +
           "i.totalOrderCount as totalOrderCount " +
           "FROM Item i JOIN i.category c ORDER BY i.totalOrderCount DESC")
    List<ItemProjection> findBestSellerItems(Pageable pageable);
}