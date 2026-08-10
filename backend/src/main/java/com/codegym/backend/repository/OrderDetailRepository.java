package com.codegym.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.codegym.backend.entity.OrderDetail;
import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.enums.StatusOrderDetail;
import com.codegym.backend.enums.StatusTableOrder;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findByOrder(TableOrder order);

    List<OrderDetail> findByOrderTableOrderId(Long orderId);

    List<OrderDetail> findByOrderTableOrderIdAndItemItemIdAndStatus(
            Long orderId, 
            Long itemId, 
            StatusOrderDetail status
    );

    List<OrderDetail> findByOrderTableOrderIdAndStatus(Long orderId, StatusOrderDetail status);

    List<OrderDetail> findByOrderTableOrderIdAndStatusNot(Long orderId, StatusOrderDetail status);

    // 🟢 Dùng phương thức này trong FeedbackServiceImpl để kiểm tra món có trong đơn hàng (không cần kiểm tra PAID)
    boolean existsByOrderTableOrderIdAndItemItemId(Long orderId, Long itemId);

    // 💡 Kiểm tra xem Khách hàng đã từng MUA & THANH TOÁN món này thành công chưa
    @Query("SELECT COUNT(od) > 0 FROM OrderDetail od " +
           "WHERE od.order.customer.customerId = :customerId " +
           "AND od.item.itemId = :itemId " +
           "AND od.order.status = :status")
    boolean existsByCustomerAndItemAndOrderStatus(
            @Param("customerId") Long customerId,
            @Param("itemId") Long itemId,
            @Param("status") StatusTableOrder status
    );

    // 🟢 Thống kê doanh thu theo từng danh mục
    @Query("SELECT c.categoryId, c.categoryName, SUM(od.quantity * od.unitPrice) " +
           "FROM OrderDetail od " +
           "JOIN od.item i " +
           "JOIN i.category c " +
           "JOIN od.order o " +
           "WHERE o.status = com.codegym.backend.enums.StatusTableOrder.PAID " +
           "GROUP BY c.categoryId, c.categoryName")
    List<Object[]> getSalesByCategories();
}