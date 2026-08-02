package com.codegym.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.enums.StatusTableOrder;

public interface TableOrderRepository extends JpaRepository<TableOrder, Long> {

    // 🟢 Dùng luôn hàm có sẵn này
    Optional<TableOrder> findByTableTableIdAndStatus(Long tableId, StatusTableOrder status);

    Optional<TableOrder> findByTableOrderIdAndTableTableIdAndStatus(
        Long tableOrderId,
        Long tableId,
        StatusTableOrder status
    );

    @Modifying
    @Transactional
    @Query("UPDATE OrderDetail od " +
           "SET od.status = com.codegym.backend.enums.StatusOrderDetail.ORDERED " +
           "WHERE od.order.tableOrderId = :tableOrderId " +
           "AND od.status = com.codegym.backend.enums.StatusOrderDetail.PENDING")
    int updatePendingItemsToOrdered(@Param("tableOrderId") Long tableOrderId);
}