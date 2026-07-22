package com.codegym.backend.service;

import com.codegym.backend.dto.CartResponseDTO;
import com.codegym.backend.dto.TableOrderInvoiceDTO;
import com.codegym.backend.dto.TableOrderSummaryDTO;
import com.codegym.backend.entity.Tables;
import com.codegym.backend.enums.PaymentMethod;
import com.codegym.backend.enums.ServiceStatus;

public interface CustomerOrderService {

    // --- 1. NHÓM QUẢN LÝ BÀN & THÔNG TIN BAN ĐẦU ---
    Tables getTableInfo(Long tableId);

    void updateTableServiceStatus(Long tableId, ServiceStatus status);


    // --- 2. NHÓM QUẢN LÝ GIỎ HÀNG TẠM (PENDING) ---
    void addItemToCart(Long tableId, Long itemId, Integer quantity, String note);

    CartResponseDTO getCartOverview(Long tableId);

    void updateCartItemDetail(Long tableId, Long itemId, Integer newQuantity, String newNote);

    void removeItemFromCart(Long tableId, Long itemId);

    void clearTemporaryCart(Long tableId);


    // --- 3. NHÓM XÁC NHẬN & XỬ LÝ ĐƠN HÀNG (GỬI BẾP/PHỤC VỤ) ---
    void confirmOrder(Long tableId);

    void markItemAsServed(Long orderDetailId);

    void cancelOrderItem(Long orderDetailId, String reason);


    // --- 4. NHÓM THANH TOÁN & HOÀN TẤT ---
    void requestCheckout(Long tableId, PaymentMethod paymentMethod);

    TableOrderSummaryDTO getInvoiceSummaryDTO(Long tableId);

    void completeCheckout(Long tableId, PaymentMethod paymentMethod);

    TableOrderInvoiceDTO getCurrentInvoice(Long tableId, Long tableOrderId);
}