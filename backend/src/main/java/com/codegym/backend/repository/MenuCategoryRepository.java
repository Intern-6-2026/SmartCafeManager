package com.codegym.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.codegym.backend.entity.MenuCategory;
import java.util.Optional;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {
    
    // Đã đổi thành findByCategoryName để khớp với trường "categoryName" trong entity MenuCategory của anh
    Optional<MenuCategory> findByCategoryName(String categoryName);
}