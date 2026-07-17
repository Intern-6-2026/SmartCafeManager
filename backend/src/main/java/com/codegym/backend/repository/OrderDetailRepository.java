package com.codegym.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codegym.backend.entity.OrderDetail;
import com.codegym.backend.enums.StatusOrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findByOrderTableOrderId(Long orderId);

    List<OrderDetail> findByOrderTableOrderIdAndItemItemIdAndStatus(Long orderId, Long itemId,
            StatusOrderDetail status);

    List<OrderDetail> findByOrderTableOrderIdAndStatus(Long orderId, StatusOrderDetail status);

    List<OrderDetail> findByOrderTableOrderIdAndStatusNot(Long orderId, StatusOrderDetail status);
}