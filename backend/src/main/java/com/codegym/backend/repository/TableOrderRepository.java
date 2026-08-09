package com.codegym.backend.repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.enums.StatusTableOrder;

public interface TableOrderRepository extends JpaRepository<TableOrder, Long> {
    // 🟢 1. Lấy đơn hàng active của bàn
    Optional<TableOrder> findByTableTableIdAndStatus(Long tableId, StatusTableOrder status);

    // 🟢 2. Lấy đơn hàng theo orderId, tableId và status
    Optional<TableOrder> findByTableOrderIdAndTableTableIdAndStatus(
            Long tableOrderId,
            Long tableId,
            StatusTableOrder status);

    // 🟢 3. Chuyển trạng thái món tạm (PENDING) sang đã đặt (ORDERED)
    @Modifying
    @Transactional
    @Query("UPDATE OrderDetail od " +
           "SET od.status = com.codegym.backend.enums.StatusOrderDetail.ORDERED " +
           "WHERE od.order.tableOrderId = :tableOrderId " +
           "AND od.status = com.codegym.backend.enums.StatusOrderDetail.PENDING")
    int updatePendingItemsToOrdered(@Param("tableOrderId") Long tableOrderId);

    // 🟢 4. Lấy danh sách hóa đơn theo bàn và trạng thái (Sắp xếp theo openAt)
    @Query("SELECT o FROM TableOrder o WHERE o.status = :status " +
           "AND (:tableId IS NULL OR o.table.tableId = :tableId) " +
           "ORDER BY o.openAt DESC")
    List<TableOrder> findInvoicesByTableAndStatus(
            @Param("tableId") Long tableId,
            @Param("status") StatusTableOrder status);

    // 🟢 5a. Thống kê tổng doanh thu (Hỗ trợ LocalDateTime - Chuẩn Spring Boot 3)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM TableOrder o " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "AND o.paidAt BETWEEN :startDate AND :endDate")
    Double sumRevenueBetween(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

    // 🟢 5b. Overload cho phép truyền java.util.Date nếu Controller cũ đang dùng Date
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM TableOrder o " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "AND o.paidAt BETWEEN :startDate AND :endDate")
    Double sumRevenueBetween(
            @Param("startDate") Date startDate, 
            @Param("endDate") Date endDate);

    // 🟢 6a. Đếm số hóa đơn trong khoảng thời gian (LocalDateTime)
    @Query("SELECT COUNT(o) FROM TableOrder o " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "AND o.paidAt BETWEEN :startDate AND :endDate")
    Long countOrdersBetween(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);

    // 🟢 6b. Overload đếm số hóa đơn (Date)
    @Query("SELECT COUNT(o) FROM TableOrder o " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "AND o.paidAt BETWEEN :startDate AND :endDate")
    Long countOrdersBetween(
            @Param("startDate") Date startDate, 
            @Param("endDate") Date endDate);

    // 🟢 7. Thống kê số lượng sản phẩm bán ra theo Danh mục
    @Query("SELECT c.categoryName, SUM(od.quantity) " +
           "FROM OrderDetail od " +
           "JOIN od.item i " +
           "JOIN i.category c " +
           "JOIN od.order o " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "GROUP BY c.categoryId, c.categoryName")
    List<Object[]> getSalesGroupedByCategory();
    // Bổ sung vào TableOrderRepository.java sẵn có
        @Query("SELECT o FROM TableOrder o " +
        "LEFT JOIN FETCH o.table t " +
        "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
        "AND (:tableId IS NULL OR t.tableId = :tableId) " +
        "AND o.paidAt BETWEEN :startDate AND :endDate " +
        "ORDER BY o.paidAt DESC")
        List<TableOrder> findInvoicesByTableAndDateRange(
        @Param("tableId") Long tableId,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate);
        // Bổ sung vào TableOrderRepository.java
        @Query("SELECT o FROM TableOrder o " +
        "LEFT JOIN FETCH o.table t " +
        "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
        "AND (:tableId IS NULL OR t.tableId = :tableId) " +
        "AND o.paidAt BETWEEN :startDate AND :endDate " +
        "ORDER BY o.paidAt DESC")
        List<TableOrder> findInvoicesByTableAndDateRange(
        @Param("tableId") Long tableId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
}