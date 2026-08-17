package com.codegym.backend.repository;

import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.enums.StatusTableOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TableOrderRepository extends JpaRepository<TableOrder, Long> {

    // ==========================================
    // 1. CHỨC NĂNG BÀN VÀ ĐẶT MÓN (ORDERING)
    // ==========================================

    Optional<TableOrder> findByTableTableIdAndStatus(Long tableId, StatusTableOrder status);

    // 🟢 MỚI: Bổ sung tìm theo nhiều trạng thái (Ví dụ: Cả OPEN và PAYMENT_REQUESTED)
    Optional<TableOrder> findByTableTableIdAndStatusIn(Long tableId, Collection<StatusTableOrder> statuses);

    Optional<TableOrder> findByTableOrderIdAndTableTableIdAndStatus(
            Long tableOrderId,
            Long tableId,
            StatusTableOrder status);

    @Modifying
    @Transactional
    @Query("UPDATE OrderDetail od " +
           "SET od.status = com.codegym.backend.enums.StatusOrderDetail.ORDERED " +
           "WHERE od.order.tableOrderId = :tableOrderId " +
           "AND od.status = com.codegym.backend.enums.StatusOrderDetail.PENDING")
    int updatePendingItemsToOrdered(@Param("tableOrderId") Long tableOrderId);


    // ==========================================
    // 2. CHỨC NĂNG XEM HÓA ĐƠN (INVOICES)
    // ==========================================

    @Query("SELECT o FROM TableOrder o WHERE o.status = :status " +
           "AND (:tableId IS NULL OR o.table.tableId = :tableId) " +
           "ORDER BY o.openAt DESC")
    List<TableOrder> findInvoicesByTableAndStatus(
            @Param("tableId") Long tableId,
            @Param("status") StatusTableOrder status);

    @Query("SELECT o FROM TableOrder o " +
           "LEFT JOIN FETCH o.table t " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "AND (:tableId IS NULL OR t.tableId = :tableId) " +
           "AND COALESCE(o.paidAt, o.createdAt) BETWEEN :startDate AND :endDate " +
           "ORDER BY o.paidAt DESC")
    List<TableOrder> findInvoicesByTableAndDateRange(
            @Param("tableId") Long tableId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM TableOrder o " +
           "LEFT JOIN FETCH o.table t " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "AND (:tableId IS NULL OR t.tableId = :tableId) " +
           "AND COALESCE(o.paidAt, o.createdAt) BETWEEN :startDate AND :endDate " +
           "ORDER BY o.paidAt DESC")
    List<TableOrder> findInvoicesByTableAndDateRange(
            @Param("tableId") Long tableId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);


    // ==========================================
    // 3. THỐNG KÊ DOANH THU & ĐẾM ĐƠN HÀNG (STATISTICS)
    // ==========================================

    // 3a. Tính doanh thu (Truyền explicit status)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM TableOrder o " +
           "WHERE o.status = :status " +
           "AND COALESCE(o.paidAt, o.createdAt) BETWEEN :startDate AND :endDate")
    Double sumRevenueBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") StatusTableOrder status);

    // 3b. Tính doanh thu (Mặc định PAID - LocalDateTime)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM TableOrder o " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "AND COALESCE(o.paidAt, o.createdAt) BETWEEN :startDate AND :endDate")
    Double sumRevenueBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 3c. Tính doanh thu (Mặc định PAID - java.util.Date)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM TableOrder o " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "AND COALESCE(o.paidAt, o.createdAt) BETWEEN :startDate AND :endDate")
    Double sumRevenueBetween(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);

    // 3d. Đếm số hóa đơn (Truyền explicit status)
    @Query("SELECT COUNT(o) FROM TableOrder o " +
           "WHERE o.status = :status " +
           "AND COALESCE(o.paidAt, o.createdAt) BETWEEN :startDate AND :endDate")
    Long countOrdersBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") StatusTableOrder status);

    // 3e. Đếm số hóa đơn (Mặc định PAID - LocalDateTime)
    @Query("SELECT COUNT(o) FROM TableOrder o " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "AND COALESCE(o.paidAt, o.createdAt) BETWEEN :startDate AND :endDate")
    Long countOrdersBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // 3f. Đếm số hóa đơn (Mặc định PAID - java.util.Date)
    @Query("SELECT COUNT(o) FROM TableOrder o " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "AND COALESCE(o.paidAt, o.createdAt) BETWEEN :startDate AND :endDate")
    Long countOrdersBetween(
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);


    // ==========================================
    // 4. BÁO CÁO DASHBOARD KHÁC
    // ==========================================

    @Query("SELECT c.categoryName, SUM(od.quantity) " +
           "FROM OrderDetail od " +
           "JOIN od.item i " +
           "JOIN i.category c " +
           "JOIN od.order o " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "GROUP BY c.categoryId, c.categoryName")
    List<Object[]> getSalesGroupedByCategory();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM TableOrder o " +
           "WHERE CAST(COALESCE(o.paidAt, o.createdAt) AS date) = CURRENT_DATE " +
           "AND o.status = :status")
    Double getTodayRevenue(@Param("status") StatusTableOrder status);

    @Query("SELECT COUNT(o) FROM TableOrder o " +
           "WHERE CAST(COALESCE(o.paidAt, o.createdAt) AS date) = CURRENT_DATE " +
           "AND o.status = :status")
    Long getTodayOrderCount(@Param("status") StatusTableOrder status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM TableOrder o " +
           "WHERE FUNCTION('MONTH', COALESCE(o.paidAt, o.createdAt)) = FUNCTION('MONTH', CURRENT_DATE) " +
           "AND FUNCTION('YEAR', COALESCE(o.paidAt, o.createdAt)) = FUNCTION('YEAR', CURRENT_DATE) " +
           "AND o.status = :status")
    Double getMonthlyRevenue(@Param("status") StatusTableOrder status);
}