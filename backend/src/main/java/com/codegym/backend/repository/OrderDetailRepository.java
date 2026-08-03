package com.codegym.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.codegym.backend.entity.OrderDetail;
import com.codegym.backend.enums.StatusOrderDetail;
import com.codegym.backend.enums.StatusTableOrder;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findByOrderTableOrderId(Long orderId);

    List<OrderDetail> findByOrderTableOrderIdAndItemItemIdAndStatus(Long orderId, Long itemId,
            StatusOrderDetail status);

    List<OrderDetail> findByOrderTableOrderIdAndStatus(Long orderId, StatusOrderDetail status);

    List<OrderDetail> findByOrderTableOrderIdAndStatusNot(Long orderId, StatusOrderDetail status);

    // 💡 2. Kiểm tra xem Khách hàng đã từng MUA & THANH TOÁN món này thành công
    // chưa
    @Query("SELECT COUNT(od) > 0 FROM OrderDetail od " +
            "WHERE od.order.customer.customerId = :customerId " +
            "AND od.item.itemId = :itemId " +
            "AND od.order.status = :status")
    boolean existsByCustomerAndItemAndOrderStatus(
            @Param("customerId") Long customerId,
            @Param("itemId") Long itemId,
            @Param("status") StatusTableOrder status);
}