package com.codegym.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codegym.backend.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    // Lấy feedback của món ăn cụ thể (bỏ qua các feedback đã xoá)
    List<Feedback> findByItemItemIdAndDeletedAtIsNull(Long itemId);

    // Lấy tất cả feedback active (cho Admin)
    List<Feedback> findByDeletedAtIsNull();
}