package com.codegym.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.enums.StatusTableOrder;

public interface TableOrderRepository extends JpaRepository<TableOrder, Long> {
    // Tìm hóa đơn đang mở (chưa thanh toán) của một bàn cụ thể
    Optional<TableOrder> findByTableTableIdAndStatus(Long tableId, StatusTableOrder status);
}