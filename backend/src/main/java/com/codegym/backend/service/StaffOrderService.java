package com.codegym.backend.service;

import com.codegym.backend.entity.OrderDetail;
import com.codegym.backend.entity.Tables;
import com.codegym.backend.enums.ServiceStatus;

import java.util.List;

public interface StaffOrderService {

    // Xem chi tiết danh sách món của bàn
    List<OrderDetail> getOrderDetailsByTable(Long tableId);

    // Chuyển trạng thái sang CONFIRMED (Xác nhận món)
    void confirmOrderItems(Long tableOrderId);

    // Chuyển trạng thái sang SERVED (Đã lên món)
    void markItemAsServed(Long orderDetailId);

    // Hủy món (CANCELLED) kèm lý do (Hết món, hết nguyên liệu...)
    void cancelOrderItem(Long orderDetailId, String reason);

    // Sửa số lượng/ghi chú món (Chỉ khi status = ORDERED)
    void updateOrderItem(Long orderDetailId, Integer newQuantity, String newNote);

    // Xóa món khỏi order (Chỉ khi status = ORDERED)
    void deleteOrderItem(Long orderDetailId);

    // Duyệt thanh toán tiền mặt (Chốt đơn & giải phóng bàn)
    void approveCashPayment(Long tableId);

    // Lấy thông tin & cập nhật trạng thái phục vụ của bàn
    Tables getTableInfo(Long tableId);
    void updateTableServiceStatus(Long tableId, ServiceStatus status);
}