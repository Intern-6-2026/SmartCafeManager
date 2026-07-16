package com.codegym.backend.service;

import java.util.List;

import com.codegym.backend.dto.TableOrderSummaryDTO;
import com.codegym.backend.entity.OrderDetail;
import com.codegym.backend.entity.Tables;
import com.codegym.backend.enums.PaymentMethod;
import com.codegym.backend.enums.ServiceStatus;
import com.codegym.backend.enums.StatusOrderDetail;

public interface CustomerOrderService {
    void addItemToCart(String tableName, Long itemId, Integer quantity, String note);
    void confirmOrder(String tableName);
    void updateTableServiceStatus(String tableName, ServiceStatus status);
    Tables getTableInfo(String tableName);
    List<OrderDetail> getCartByStatus(String tableName, StatusOrderDetail status);
    List<OrderDetail> getOrderedItems(String tableName);
    void requestCheckout(String tableName, PaymentMethod paymentMethod);
    TableOrderSummaryDTO getInvoiceSummaryDTO(String tableName);
    void updateItemQuantityInCart(String tableName, Long itemId, Integer newQuantity);
    void removeItemFromCart(String tableName, Long itemId);
    void clearTemporaryCart(String tableName);
}