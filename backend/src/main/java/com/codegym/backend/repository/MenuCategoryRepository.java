package com.codegym.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.codegym.backend.entity.MenuCategory;
import java.util.Optional;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

    // Đã đổi thành findByCategoryName để khớp với trường "categoryName" trong
    // entity MenuCategory của anh
    Optional<MenuCategory> findByCategoryName(String categoryName);
}