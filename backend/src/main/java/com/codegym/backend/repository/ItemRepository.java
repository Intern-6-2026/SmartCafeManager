package com.codegym.backend.repository;

import com.codegym.backend.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // 1. Tìm các món sắp xếp theo ID giảm dần (mới nhất) kèm giới hạn số lượng
    List<Item> findByOrderByItemIdDesc(Pageable pageable);

    // 2. Tìm các món sắp xếp theo số lượng bán giảm dần (bán chạy nhất) kèm giới
    // hạn số lượng
    List<Item> findByOrderByTotalOrderCountDesc(Pageable pageable);
}