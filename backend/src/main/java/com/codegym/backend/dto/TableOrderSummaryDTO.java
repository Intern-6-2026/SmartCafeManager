package com.codegym.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TableOrderSummaryDTO {
    private Long tableOrderId;
    private String tableName;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String serviceStatus;
    private LocalDateTime openAt;
    private List<OrderDetailDTO> orderDetails;

    // Getter và Setter cho class lớn
    public Long getTableOrderId() { return tableOrderId; }
    public void setTableOrderId(Long tableOrderId) { this.tableOrderId = tableOrderId; }

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getServiceStatus() { return serviceStatus; }
    public void setServiceStatus(String serviceStatus) { this.serviceStatus = serviceStatus; }

    public LocalDateTime getOpenAt() { return openAt; }
    public void setOpenAt(LocalDateTime openAt) { this.openAt = openAt; }

    public List<OrderDetailDTO> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(List<OrderDetailDTO> orderDetails) { this.orderDetails = orderDetails; }

        
    // QUAN TRỌNG: Phải có chữ "static" ở đây để Java hiểu và map mượt mà!
    public static class OrderDetailDTO {
        private Long orderDetailId;
        private Integer quantity;
        private Long unitPrice;
        private String note;
        private String status;
        private Long itemId;
        private String itemName;
        private String imageUrl;

        // Getter và Setter cho class con
        public Long getOrderDetailId() { return orderDetailId; }
        public void setOrderDetailId(Long orderDetailId) { this.orderDetailId = orderDetailId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Long getUnitPrice() { return unitPrice; }
        public void setUnitPrice(Long unitPrice) { this.unitPrice = unitPrice; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }

        public String getItemName() { return itemName; }
        public void setSimpleItemName(String itemName) { this.itemName = itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }
    
}