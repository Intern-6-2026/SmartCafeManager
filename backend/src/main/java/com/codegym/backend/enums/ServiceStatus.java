package com.codegym.backend.enums;

public enum ServiceStatus {
    EMPTY,            // Bàn trống
    NORMAL,           // Bàn hoạt động/phục vụ bình thường
    SERVING,          // Bàn đang phục vụ món
    WAITING_FOOD,     // Bàn chờ bếp làm món
    REQUESTING_BILL,  // Bàn đang yêu cầu thanh toán
    WAITING_PAYMENT   // Bàn đang chờ xử lý thanh toán
}