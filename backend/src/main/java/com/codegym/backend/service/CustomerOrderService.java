package com.codegym.backend.service;

import java.util.List;

import com.codegym.backend.dto.CartItemResponse;
import com.codegym.backend.dto.TableOrderSummaryDTO;
import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.entity.Tables;
import com.codegym.backend.enums.PaymentMethod;
import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.enums.StatusOrderDetail;

public interface CustomerOrderService {
    void addItemToCart(String tableName, Long itemId, Integer quantity, String note);// 1. Thêm món vào giỏ hàng tạm (PENDING)
    void confirmOrder(String tableName);// 2. Xác nhận gửi đơn xuống bếp (GỌI MÓN)
    void updateTableServiceStatus(String tableName, ServiceStatus status);// 3. Đổi trạng thái phục vụ của bàn (Gọi nhân viên, v.v.)
    Tables getTableInfo(String tableName);// 4. Lấy thông tin bàn
    List<CartItemResponse> getCartByStatus(String tableName, StatusOrderDetail status);// 5. Xem món trong giỏ hàng tạm (PENDING)
    List<CartItemResponse> getOrderedItems(String tableName);// 6. Xem danh sách món đã gọi xuống bếp
    TableOrder getCurrentInvoice(String tableName);// 7. Lấy thông tin hóa đơn hiện tại
    void requestCheckout(String tableName, PaymentMethod paymentMethod);// 8. Khách ấn nút Yêu cầu thanh toán
    TableOrderSummaryDTO getInvoiceSummaryDTO(String tableName);// 9. Lấy DTO xem trước hóa đơn tạm tính
    void updateItemQuantityInCart(String tableName, Long itemId, Integer newQuantity);// 10. Cập nhật số lượng món trong giỏ tạm
    void removeItemFromCart(String tableName, Long itemId); // 11. Xóa 1 món khỏi giỏ tạm
    void clearTemporaryCart(String tableName);// 12. Xóa toàn bộ món trong giỏ tạm
    void markItemAsServed(Long orderDetailId);// 13. [NGHIỆP VỤ BẾP] Đánh dấu món đã làm xong (SERVED)
    void cancelOrderItem(Long orderDetailId, String reason); // 14. [NGHIỆP VỤ BẾP] Bếp báo hủy món do hết hàng (CANCELLED)
}