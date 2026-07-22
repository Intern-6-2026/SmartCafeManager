package com.codegym.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codegym.backend.dto.CartItemResponse;
import com.codegym.backend.dto.CartResponseDTO;
import com.codegym.backend.dto.TableOrderInvoiceDTO;
import com.codegym.backend.dto.TableOrderSummaryDTO;
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

    // 1. NGHIỆP VỤ: THÊM MÓN VÀO GIỎ TẠM THỜI
    @Override
    @Transactional
    public void addItemToCart(Long tableId, Long itemId, Integer quantity, String note) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Món ăn không tồn tại với ID: " + itemId));

        // Tìm hóa đơn đang OPEN của bàn này, nếu chưa có thì mở mới
        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseGet(() -> {
                    table.setIsOccupied(true);
                    tablesRepository.save(table);

                    TableOrder newOrder = TableOrder.builder()
                            .table(table)
                            .openAt(LocalDateTime.now())
                            .totalAmount(BigDecimal.ZERO)
                            .status(StatusTableOrder.OPEN)
                            .build();
                    return tableOrderRepository.save(newOrder);
                });

        // Thêm/cập nhật món vào giỏ PENDING
        List<OrderDetail> existingDetails = orderDetailRepository
                .findByOrderTableOrderIdAndItemItemIdAndStatus(order.getTableOrderId(), itemId, StatusOrderDetail.PENDING);

        if (!existingDetails.isEmpty()) {
            OrderDetail detail = existingDetails.get(0);
            detail.setQuantity(detail.getQuantity() + quantity);
            if (note != null && !note.trim().isEmpty()) {
                detail.setNote(note);
            }
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

    // 2. NGHIỆP VỤ: LẤY TOÀN BỘ TỔNG QUAN GIỎ HÀNG (MÓN ĐÃ GỌI + MÓN TẠM)
    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCartOverview(Long tableId) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));

        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElse(null);

        if (order == null) {
            return CartResponseDTO.builder()
                    .tableId(table.getTableId())
                    .tableName(table.getTableName())
                    .currentTotalAmount(BigDecimal.ZERO)
                    .orderedItems(List.of())
                    .pendingItems(List.of())
                    .build();
        }

        List<OrderDetail> allDetails = orderDetailRepository.findByOrderTableOrderId(order.getTableOrderId());

        // 1. Món trong giỏ tạm (PENDING)
        List<CartItemResponse> pendingItems = allDetails.stream()
                .filter(d -> d.getStatus() == StatusOrderDetail.PENDING)
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        // 2. Món đã gửi xuống bếp (CONFIRMED, SERVED...)
        List<CartItemResponse> orderedItems = allDetails.stream()
                .filter(d -> d.getStatus() != StatusOrderDetail.PENDING && d.getStatus() != StatusOrderDetail.CANCELLED)
                .map(this::mapToCartItemResponse)
                .collect(Collectors.toList());

        return CartResponseDTO.builder()
                .tableOrderId(order.getTableOrderId())
                .tableId(table.getTableId())
                .tableName(table.getTableName())
                .currentTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO)
                .orderedItems(orderedItems)
                .pendingItems(pendingItems)
                .build();
    }

    // 3. CẬP NHẬT SỐ LƯỢNG VÀ GHI CHÚ TRONG GIỎ HÀNG TẠM (PENDING)
    @Override
    @Transactional
    public void updateCartItemDetail(Long tableId, Long itemId, Integer newQuantity, String newNote) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));

        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn đang mở cho bàn này!"));

        List<OrderDetail> existingDetails = orderDetailRepository
                .findByOrderTableOrderIdAndItemItemIdAndStatus(order.getTableOrderId(), itemId, StatusOrderDetail.PENDING);

        if (existingDetails.isEmpty()) {
            throw new RuntimeException("Món ăn không tồn tại trong giỏ hàng tạm thời!");
        }

        OrderDetail detail = existingDetails.get(0);

        if (newQuantity != null && newQuantity > 0) {
            detail.setQuantity(newQuantity);
        }
        if (newNote != null) {
            detail.setNote(newNote);
        }

        orderDetailRepository.save(detail);
    }

    // 4. XÓA MỘT MÓN CỤ THỂ KHỎI GIỎ HÀNG TẠM (PENDING)
    @Override
    @Transactional
    public void removeItemFromCart(Long tableId, Long itemId) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));

        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn đang mở cho bàn này!"));

        List<OrderDetail> existingDetails = orderDetailRepository
                .findByOrderTableOrderIdAndItemItemIdAndStatus(order.getTableOrderId(), itemId, StatusOrderDetail.PENDING);

        if (!existingDetails.isEmpty()) {
            orderDetailRepository.delete(existingDetails.get(0));
        }
    }

    // 5. XÓA SẠCH TẤT CẢ MÓN TRONG GIỎ HÀNG TẠM (PENDING)
    @Override
    @Transactional
    public void clearTemporaryCart(Long tableId) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));

        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn đang mở cho bàn này!"));

        List<OrderDetail> pendingDetails = orderDetailRepository
                .findByOrderTableOrderIdAndStatus(order.getTableOrderId(), StatusOrderDetail.PENDING);

        if (!pendingDetails.isEmpty()) {
            orderDetailRepository.deleteAll(pendingDetails);
        }
    }

    // 6. NGHIỆP VỤ: BẤM NÚT [GỌI MÓN] ĐỂ XÁC NHẬN GỬI XUỐNG BẾP
    @Override
    @Transactional
    public void confirmOrder(Long tableId) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));

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
            
            if (detail.getStatus() == StatusOrderDetail.CONFIRMED || detail.getStatus() == StatusOrderDetail.SERVED) {
                BigDecimal itemTotal = detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }
        }

        order.setTotalAmount(totalAmount);
        tableOrderRepository.save(order);

        table.setServiceStatus(ServiceStatus.WAITING_FOOD);
        tablesRepository.save(table);
    }

    // 7. BẤM NÚT [GỌI NHÂN VIÊN] HOẶC THAY ĐỔI TRẠNG THÁI DỊCH VỤ
    @Override
    @Transactional
    public void updateTableServiceStatus(Long tableId, ServiceStatus status) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));
        table.setServiceStatus(status);
        tablesRepository.save(table);
    }

    // 8. LẤY THÔNG TIN BÀN
    @Override
    @Transactional(readOnly = true)
    public Tables getTableInfo(Long tableId) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));
        return table;
    }

    // 9. KHÁCH ẤN NÚT YÊU CẦU THANH TOÁN
    @Override
    @Transactional
    public void requestCheckout(Long tableId, PaymentMethod paymentMethod) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));

        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn cần thanh toán!"));

        order.setPaymentMethod(paymentMethod);
        tableOrderRepository.save(order);

        table.setServiceStatus(ServiceStatus.REQUESTING_BILL);
        tablesRepository.save(table);
    }

    // 10. XEM CHI TIẾT HÓA ĐƠN TẠM TÍNH (CHO MÀN HÌNH XÁC NHẬN THANH TOÁN)
    @Override
    @Transactional(readOnly = true)
    public TableOrderSummaryDTO getInvoiceSummaryDTO(Long tableId) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));

        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Bàn hiện tại không có hóa đơn nào đang mở!"));

        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(order.getTableOrderId());

        BigDecimal calculatedTotal = details.stream()
                .filter(d -> d.getStatus() != StatusOrderDetail.CANCELLED)
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TableOrderSummaryDTO.OrderDetailDTO> detailDTOs = details.stream().map(d -> {
            TableOrderSummaryDTO.OrderDetailDTO dto = new TableOrderSummaryDTO.OrderDetailDTO();
            dto.setOrderDetailId(d.getOrderDetailId());
            dto.setQuantity(d.getQuantity());
            dto.setUnitPrice(d.getUnitPrice().longValue());
            dto.setNote(d.getNote());
            dto.setStatus(d.getStatus().name());
            dto.setItemId(d.getItem() != null ? d.getItem().getItemId() : null);
            dto.setItemName(d.getItem() != null ? d.getItem().getItemName() : null);
            dto.setImageUrl(d.getItem() != null ? d.getItem().getImageUrl() : null);
            return dto;
        }).collect(Collectors.toList());

        TableOrderSummaryDTO summary = new TableOrderSummaryDTO();
        summary.setTableOrderId(order.getTableOrderId());
        summary.setTableName(table.getTableName());
        summary.setTotalAmount(calculatedTotal);
        summary.setOrderStatus(order.getStatus().name());
        summary.setServiceStatus(table.getServiceStatus().name());
        summary.setOpenAt(order.getOpenAt());
        summary.setOrderDetails(detailDTOs);

        return summary;
    }

    // 11. BẾP CHẾ BIẾN XONG -> CHUYỂN TRẠNG THÁI SANG SERVED
    @Override
    @Transactional
    public void markItemAsServed(Long orderDetailId) {
        OrderDetail detail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết đơn hàng với ID: " + orderDetailId));
        detail.setStatus(StatusOrderDetail.SERVED);
        orderDetailRepository.save(detail);
    }

    // 12. BẾP HẾT MÓN -> CHUYỂN TRẠNG THÁI SANG CANCELLED
    @Override
    @Transactional
    public void cancelOrderItem(Long orderDetailId, String reason) {
        OrderDetail detail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết đơn hàng với ID: " + orderDetailId));

        detail.setStatus(StatusOrderDetail.CANCELLED);
        detail.setNote("Bếp hủy: " + (reason != null && !reason.trim().isEmpty() ? reason : "Hết món"));
        orderDetailRepository.save(detail);

        TableOrder order = detail.getOrder();
        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(order.getTableOrderId());

        BigDecimal newTotal = details.stream()
                .filter(d -> d.getStatus() == StatusOrderDetail.CONFIRMED || d.getStatus() == StatusOrderDetail.SERVED)
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(newTotal);
        tableOrderRepository.save(order);
    }

    // 13. HOÀN TẤT THANH TOÁN, DỌN GIỎ HÀNG TẠM & GIẢI PHÓNG BÀN
    @Override
    @Transactional
    public void completeCheckout(Long tableId, PaymentMethod paymentMethod) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));

        TableOrder order = tableOrderRepository.findByTableTableIdAndStatus(table.getTableId(), StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException("Bàn ID " + tableId + " hiện không có hóa đơn nào cần thanh toán!"));

        List<OrderDetail> pendingDetails = orderDetailRepository
                .findByOrderTableOrderIdAndStatus(order.getTableOrderId(), StatusOrderDetail.PENDING);
        if (!pendingDetails.isEmpty()) {
            orderDetailRepository.deleteAll(pendingDetails);
        }

        order.setPaymentMethod(paymentMethod);
        order.setStatus(StatusTableOrder.PAID);
        order.setCloseAt(LocalDateTime.now());
        tableOrderRepository.save(order);

        table.setIsOccupied(false);
        table.setServiceStatus(ServiceStatus.NORMAL);
        tablesRepository.save(table);
    }

    // 14. HÀM TRA CỨU HÓA ĐƠN THEO ID MỜI (CHO ADMIN/THU NGÂN)
    @Override
    @Transactional(readOnly = true)
    public TableOrderInvoiceDTO getCurrentInvoice(Long tableId, Long tableOrderId) {
        Tables table = tablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Bàn không tồn tại với ID: " + tableId));

        TableOrder order = tableOrderRepository
                .findByTableOrderIdAndTableTableIdAndStatus(tableOrderId, tableId, StatusTableOrder.OPEN)
                .orElseThrow(() -> new RuntimeException(
                    "Không tìm thấy hóa đơn mở ID #" + tableOrderId + " tại bàn ID " + tableId
                ));

        List<OrderDetail> details = orderDetailRepository.findByOrderTableOrderId(order.getTableOrderId());

        List<TableOrderInvoiceDTO.OrderItemDTO> itemDTOs = details.stream().map(d -> {
            BigDecimal totalPrice = d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity()));
            return TableOrderInvoiceDTO.OrderItemDTO.builder()
                    .orderDetailId(d.getOrderDetailId())
                    .itemId(d.getItem() != null ? d.getItem().getItemId() : null)
                    .itemName(d.getItem() != null ? d.getItem().getItemName() : null)
                    .quantity(d.getQuantity())
                    .unitPrice(d.getUnitPrice())
                    .totalPrice(totalPrice)
                    .note(d.getNote())
                    .status(d.getStatus().name())
                    .imageUrl(d.getItem() != null ? d.getItem().getImageUrl() : null)
                    .build();
        }).collect(Collectors.toList());

        return TableOrderInvoiceDTO.builder()
                .tableOrderId(order.getTableOrderId())
                .tableId(table.getTableId())
                .tableName(table.getTableName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .openAt(order.getOpenAt())
                .closeAt(order.getCloseAt())
                .orderDetails(itemDTOs)
                .build();
    }

    private CartItemResponse mapToCartItemResponse(OrderDetail detail) {
        return CartItemResponse.builder()
                .orderDetailId(detail.getOrderDetailId())
                .itemId(detail.getItem() != null ? detail.getItem().getItemId() : null)
                .itemName(detail.getItem() != null ? detail.getItem().getItemName() : null)
                .price(detail.getUnitPrice())
                .quantity(detail.getQuantity())
                .note(detail.getNote())
                .status(detail.getStatus() != null ? detail.getStatus().name() : null)
                .tableName(detail.getOrder() != null && detail.getOrder().getTable() != null 
                        ? detail.getOrder().getTable().getTableName() : null)
                .build();
    }
}