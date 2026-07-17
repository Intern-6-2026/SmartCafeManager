package com.codegym.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codegym.backend.entity.Item;
import com.codegym.backend.entity.OrderDetail;
import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.entity.Tables;
import com.codegym.backend.enums.PaymentMethod;
import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.enums.StatusOrderDetail;
import com.codegym.backend.enums.StatusTableOrder;
import com.codegym.backend.repository.ItemRepository;
import com.codegym.backend.repository.OrderDetailRepository;
import com.codegym.backend.repository.TableOrderRepository;
import com.codegym.backend.repository.TablesRepository;

@Service
public class CustomerOrderServiceImpl implements CustomerOrderService {

    @Autowired private TablesRepository tablesRepository;
    @Autowired private TableOrderRepository tableOrderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private ItemRepository itemRepository;

    // 1. NGHIỆP VỤ: THÊM MÓN VÀO GIỎ TẠM THỜI (Popup nhập số lượng)
    @Override
    @Transactional
    public void addItemToCart(String tableName, Long itemId, Integer quantity, String note) {
        Tables table = tablesRepository.findByTableName(tableName)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại!"));
        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseGet(() -> {
                    table.setIsOccupied(true);
                    tablesRepository.save(table);

                    TableOrder newOrder = TableOrder.builder()
                            .table(table)
                            .openAt(LocalDateTime.now())
                            .closeAt(null) // Cho phép NULL dưới DB theo lệnh ALTER TABLE chúng ta vừa chạy
                            .totalAmount(BigDecimal.ZERO)
                            .status(StatusTableOrder.OPEN)
                            .build();
                    return tableOrderRepository.save(newOrder);
                });
        List<OrderDetail> existingDetails = orderDetailRepository
                .findByOrderTableOrderIdAndItemItemIdAndStatus(order.getTableOrderId(), itemId, StatusOrderDetail.PENDING);

        if (!existingDetails.isEmpty()) {
            OrderDetail detail = existingDetails.get(0);
            detail.setQuantity(detail.getQuantity() + quantity);
            orderDetailRepository.save(detail);
        } else {
            OrderDetail newDetail = OrderDetail.builder()
                    .order(order)
                    .item(item)
                    .quantity(quantity)
                    .unitPrice(item.getPrice()) 
                    .note(note)
                    .status(StatusOrderDetail.PENDING)
                    .build();

            orderDetailRepository.save(newDetail);
        }
    }

    // 2. NGHIỆP VỤ: BẤM NÚT [GỌI MÓN] ĐỂ XÁC NHẬN GỬI XUỐNG BẾP
    @Override
    @Transactional
    public void confirmOrder(String tableName) {
        Tables table = tablesRepository.findByTableName(tableName)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn đang mở cho bàn này!"));

        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(order.getTableOrderId());
        
        if (details.isEmpty()) {
            throw new RuntimeException("Giỏ hàng của bạn đang trống, không thể gọi món!");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderDetail detail : details) {
            if (detail.getStatus() == StatusOrderDetail.PENDING) {
                detail.setStatus(StatusOrderDetail.CONFIRMED);
                orderDetailRepository.save(detail);
                
                Item item = detail.getItem();
                int currentCount = item.getTotalOrderCount() != null ? item.getTotalOrderCount() : 0;
                item.setTotalOrderCount(currentCount + detail.getQuantity());
                itemRepository.save(item);
            }
            
            // Tính tổng tiền chuẩn xác cho TOÀN BỘ các món đã được bếp xác nhận hoặc đã phục vụ tại bàn
            if (detail.getStatus() == StatusOrderDetail.CONFIRMED || detail.getStatus() == StatusOrderDetail.SERVED) {
                BigDecimal itemTotal = detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }
        }

        // Cập nhật lại tổng tiền chính xác của hóa đơn
        order.setTotalAmount(totalAmount);
        tableOrderRepository.save(order);

        // Chuyển trạng thái phục vụ của bàn sang "Đang chờ món"
        table.setServiceStatus(ServiceStatus.WAITING_FOOD);
        tablesRepository.save(table);
    }

    // 3. NGHIỆP VỤ: BẤM NÚT [GỌI NHÂN VIÊN] HOẶC THAY ĐỔI TRẠNG THÁI TRỰC TIẾP
    @Override
    @Transactional
    public void updateTableServiceStatus(String tableName, ServiceStatus status) {
        Tables table = tablesRepository.findByTableName(tableName)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));
        table.setServiceStatus(status);
        tablesRepository.save(table);
    }

    // 4. NGHIỆP VỤ: LẤY THÔNG TIN BÀN
    @Override
    @Transactional(readOnly = true)
    public Tables getTableInfo(String tableName) {
        return tablesRepository.findByTableName(tableName)
                .orElseThrow(() -> new RuntimeException("Bàn này không tồn tại trong hệ thống!"));
    }

   // 5. NGHIỆP VỤ: XEM TẤT CẢ MÓN TRONG GIỎ HÀNG TẠM THEO DTO SẠCH
   @Override
   @Transactional(readOnly = true)
   public List<com.codegym.backend.dto.CartItemResponse> getCartByStatus(String tableName, StatusOrderDetail status) {
       Tables table = tablesRepository.findByTableName(tableName)
               .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

       TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
               .orElseThrow(() -> new RuntimeException("Bàn hiện tại không có hóa đơn nào đang mở!"));

       List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderIdAndStatus(order.getTableOrderId(), status);

       // Map sang DTO sạch sẽ vừa khai báo ở trên để bẻ gãy Hibernate Proxy lỗi
       return details.stream().map(d -> com.codegym.backend.dto.CartItemResponse.builder()
               .orderDetailId(d.getOrderDetailId())
               .itemId(d.getItem() != null ? d.getItem().getItemId() : null)
               .itemName(d.getItem() != null ? d.getItem().getItemName() : null)
               .price(d.getUnitPrice())
               .quantity(d.getQuantity())
               .tableName(tableName)
               .build()
       ).collect(Collectors.toList());
   }

   // 6. NGHIỆP VỤ: XEM TẤT CẢ CÁC MÓN ĐÃ GỌI XUỐNG BẾP THEO DTO SẠCH
   @Override
   @Transactional(readOnly = true)
   public List<com.codegym.backend.dto.CartItemResponse> getOrderedItems(String tableName) {
       Tables table = tablesRepository.findByTableName(tableName)
               .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

       TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
               .orElseThrow(() -> new RuntimeException("Bàn hiện tại không có hóa đơn nào đang mở!"));

       List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderIdAndStatusNot(order.getTableOrderId(), StatusOrderDetail.PENDING);

       return details.stream().map(d -> com.codegym.backend.dto.CartItemResponse.builder()
               .orderDetailId(d.getOrderDetailId())
               .itemId(d.getItem() != null ? d.getItem().getItemId() : null)
               .itemName(d.getItem() != null ? d.getItem().getItemName() : null)
               .price(d.getUnitPrice())
               .quantity(d.getQuantity())
               .tableName(tableName)
               .build()
       ).collect(Collectors.toList());
   }
    // 7. NGHIỆP VỤ: XEM CHI TIẾT HÓA ĐƠN LỚN (Để lấy tổng tiền cho khách xem trước khi thanh toán)
    //@Override
    @Transactional(readOnly = true)
    public TableOrder getCurrentInvoice(String tableName) {
        Tables table = tablesRepository.findByTableName(tableName)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

        return tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Bàn hiện tại không có hóa đơn nào chưa thanh toán!"));
    }

    // 8. NGHIỆP VỤ: KHÁCH ẤN NÚT YÊU CẦU THANH TOÁN (Chọn hình thức CASH, BANK_TRANSFER,...)
    @Override
    @Transactional
    public void requestCheckout(String tableName, PaymentMethod paymentMethod) {
        Tables table = tablesRepository.findByTableName(tableName)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn cần thanh toán!"));

        // Thiết lập phương thức thanh toán khách chọn
        order.setPaymentMethod(paymentMethod);
        tableOrderRepository.save(order);

        // Chuyển trạng thái bàn sang "Yêu cầu tính tiền" khớp 100% với Enum ServiceStatus của bạn
        table.setServiceStatus(ServiceStatus.REQUESTING_BILL);
        tablesRepository.save(table);
    }
    // 9. NGHIỆP VỤ MỚI: LẤY TỔNG QUAN HÓA ĐƠN ĐÃ ĐƯỢC CHUYỂN ĐỔI SANG DTO SẠCH SẼ
// 9. NGHIỆP VỤ MỚI: LẤY TỔNG QUAN HÓA ĐƠN ĐÃ ĐƯỢC CHUYỂN ĐỔI SANG DTO SẠCH SẼ
@Override
@Transactional(readOnly = true)
public com.codegym.backend.dto.TableOrderSummaryDTO getInvoiceSummaryDTO(String tableName) {
    // BƯỚC 1: Tìm kiếm thông tin bàn
    Tables table = tablesRepository.findByTableName(tableName)
            .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

    // BƯỚC 2: Tìm kiếm hóa đơn đang OPEN của bàn đó
    TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
            .orElseThrow(() -> new RuntimeException("Bàn hiện tại không có hóa đơn nào chưa thanh toán!"));

    // BƯỚC 3: Lấy toàn bộ danh sách món ăn thuộc hóa đơn này
    List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(order.getTableOrderId());

    // BƯỚC 4: Tự động tính toán tổng tiền thực tế thời gian thực (Bao gồm cả PENDING, CONFIRMED, SERVED)
    BigDecimal calculatedTotal = details.stream()
            .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // BƯỚC 5: Đưa vào Builder của TableOrderSummaryDTO và map sang list con phẳng
    return com.codegym.backend.dto.TableOrderSummaryDTO.builder()
            .tableOrderId(order.getTableOrderId())
            .tableName(table.getTableName())
            .totalAmount(calculatedTotal)
            .orderStatus(order.getStatus())
            .serviceStatus(table.getServiceStatus())
            .openAt(order.getOpenAt())
            .orderDetails(details.stream().map(d -> {
                return com.codegym.backend.dto.OrderDetailMinDTO.builder()
                        .orderDetailId(d.getOrderDetailId())
                        .quantity(d.getQuantity())
                        .unitPrice(d.getUnitPrice())
                        .note(d.getNote())
                        .status(d.getStatus().toString())
                        .itemId(d.getItem() != null ? d.getItem().getItemId() : null)
                        .itemName(d.getItem() != null ? d.getItem().getItemName() : null)
                        .imageUrl(d.getItem() != null ? d.getItem().getImageUrl() : null)
                        .build();
            }).collect(java.util.stream.Collectors.toList()))
            .build();
}
// 10. NGHIỆP VỤ MỚI: CẬP NHẬT SỐ LƯỢNG MÓN ĂN TRONG GIỎ HÀNG TẠM (PENDING)
@Override
@Transactional
public void updateItemQuantityInCart(String tableName, Long itemId, Integer newQuantity) {
    Tables table = tablesRepository.findByTableName(tableName)
            .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

    TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn đang mở cho bàn này!"));

    List<OrderDetail> existingDetails = orderDetailRepository
            .findByOrderTableOrderIdAndItemItemIdAndStatus(order.getTableOrderId(), itemId, StatusOrderDetail.PENDING);

    if (existingDetails.isEmpty()) {
        throw new RuntimeException("Món ăn không tồn tại trong giỏ hàng tạm thời!");
    }

    // Cập nhật số lượng mới cứng do Front-end truyền xuống
    OrderDetail detail = existingDetails.get(0);
    detail.setQuantity(newQuantity);
    orderDetailRepository.save(detail);
}

// 11. NGHIỆP VỤ MỚI: XÓA MỘT MÓN CỤ THỂ KHỎI GIỎ HÀNG TẠM (PENDING)
@Override
@Transactional
public void removeItemFromCart(String tableName, Long itemId) {
    Tables table = tablesRepository.findByTableName(tableName)
            .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

    TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn đang mở cho bàn này!"));

    List<OrderDetail> existingDetails = orderDetailRepository
            .findByOrderTableOrderIdAndItemItemIdAndStatus(order.getTableOrderId(), itemId, StatusOrderDetail.PENDING);

    if (!existingDetails.isEmpty()) {
        orderDetailRepository.delete(existingDetails.get(0));
    }
}

// 12. NGHIỆP VỤ MỚI: XÓA SẠCH TẤT CẢ MÓN TRONG GIỎ HÀNG TẠM (PENDING)
@Override
@Transactional
public void clearTemporaryCart(String tableName) {
    Tables table = tablesRepository.findByTableName(tableName)
            .orElseThrow(() -> new RuntimeException("Bàn không tồn tại!"));

    TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn đang mở cho bàn này!"));

    // Tìm tất cả các món đang PENDING của bàn này
    List<OrderDetail> pendingDetails = orderDetailRepository
            .findByOrderTableOrderIdAndStatus(order.getTableOrderId(), StatusOrderDetail.PENDING);

    if (!pendingDetails.isEmpty()) {
        orderDetailRepository.deleteAll(pendingDetails);
    }
}
}