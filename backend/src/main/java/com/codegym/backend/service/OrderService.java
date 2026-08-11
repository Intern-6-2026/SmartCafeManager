package com.codegym.backend.service;
import com.codegym.backend.enums.ServiceStatus; 
import com.codegym.backend.dto.CartResponseDTO;
import com.codegym.backend.dto.InvoiceDetailResponseDTO;


public interface OrderService {
    
    void updateTableServiceStatus(Long tableId, ServiceStatus status);

    void addItemToCart(Long tableId, Long itemId, Integer quantity, String note);
    CartResponseDTO getCartOverview(Long tableId);
    void updateCartItemDetail(Long tableId, Long itemId, Integer newQuantity, String newNote);
    void removeItemFromCart(Long tableId, Long itemId);
    void clearTemporaryCart(Long tableId);
    void confirmOrder(Long tableId);
    InvoiceDetailResponseDTO getInvoiceDetailForCustomer(Long orderId, Long customerId);
    void cancelOrderItemByCustomer(Long orderDetailId, String reason);
}