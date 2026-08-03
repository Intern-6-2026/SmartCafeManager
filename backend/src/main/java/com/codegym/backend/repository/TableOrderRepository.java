package com.codegym.backend.repository;

import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.enums.StatusTableOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TableOrderRepository extends JpaRepository<TableOrder, Long> {

    // 🟢 Lấy đơn hàng active của bàn
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

    // 1. Lấy danh sách hóa đơn (Đã sửa o.coffeeTable -> o.table)
    @Query("SELECT o FROM TableOrder o WHERE o.status = :status " +
           "AND (:tableId IS NULL OR o.table.tableId = :tableId) " +
           "ORDER BY o.createdAt DESC")
    List<TableOrder> findInvoicesByTableAndStatus(
            @Param("tableId") Long tableId,
            @Param("status") StatusTableOrder status
    );

    // 2. Thống kê tổng doanh thu trong khoảng thời gian
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM TableOrder o " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "AND o.createdAt BETWEEN :startDate AND :endDate")
    Double sumRevenueBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    // 3. Đếm số hóa đơn trong khoảng thời gian
    @Query("SELECT COUNT(o) FROM TableOrder o " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "AND o.createdAt BETWEEN :startDate AND :endDate")
    Long countOrdersBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    // 4. Thống kê số lượng sản phẩm bán ra theo Danh mục (Cho biểu đồ tròn)
    @Query("SELECT c.categoryName, SUM(od.quantity) " +
           "FROM OrderDetail od " +
           "JOIN od.item i " +
           "JOIN i.category c " +
           "JOIN od.order o " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "GROUP BY c.categoryId, c.categoryName")
    List<Object[]> getSalesGroupedByCategory();
}