
package com.codegym.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.enums.StatusTableOrder;

public interface TableOrderRepository extends JpaRepository<TableOrder, Long> {
    Optional<TableOrder> findByTableTableIdAndStatus(Long tableId, StatusTableOrder status);
    Optional<TableOrder> findByTableOrderIdAndTableTableIdAndStatus(
        Long tableOrderId,
        Long tableId,
        StatusTableOrder status
    );
}