package com.codegym.backend.service;

import com.codegym.backend.dto.DashboardStatsDTO;
import com.codegym.backend.dto.InvoiceResponseDTO;
import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.enums.StatusTableOrder;
import com.codegym.backend.repository.TableOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final TableOrderRepository tableOrderRepository;

    // --- 1. LẤY DANH SÁCH HÓA ĐƠN ---
    @Override
    public List<InvoiceResponseDTO> getInvoices(Long tableId) {
        List<TableOrder> orders = tableOrderRepository.findInvoicesByTableAndStatus(tableId, StatusTableOrder.PAID);

        return orders.stream().map(order -> InvoiceResponseDTO.builder()
                .orderId(order.getTableOrderId())
                .invoiceCode(String.format("#HD%04d", order.getTableOrderId()))
                .tableId(order.getTable() != null ? order.getTable().getTableId() : null)
                .tableName(order.getTable() != null ? order.getTable().getTableName() : "Mang về")
                .totalAmount(order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0)
                .createdAt(order.getCreatedAt())
                .status("Đã thanh toán")
                .build()
        ).collect(Collectors.toList());
    }

    // --- 2. LẤY DỮ LIỆU BẢNG THỐNG KÊ DOANH THU ---
    @Override
    public DashboardStatsDTO getDashboardStats() {
        LocalDate today = LocalDate.now();

        // Khoảng thời gian Hôm nay (00:00:00 -> 23:59:59)
        Date startOfToday = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfToday = Date.from(today.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        // Khoảng thời gian Tháng này (Đầu tháng -> Cuối tháng)
        Date startOfMonth = Date.from(today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfMonth = Date.from(today.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

        // A. 3 Thẻ Tổng quan
        Double todayRevenue = tableOrderRepository.sumRevenueBetween(startOfToday, endOfToday);
        Long todayOrderCount = tableOrderRepository.countOrdersBetween(startOfToday, endOfToday);
        Double monthRevenue = tableOrderRepository.sumRevenueBetween(startOfMonth, endOfMonth);

        // B. Biểu đồ đường theo tuần (Từ Thứ 2 -> Chủ Nhật của tuần hiện tại)
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<DashboardStatsDTO.WeeklyRevenueDTO> weeklyRevenueList = new ArrayList<>();
        String[] dayNames = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};

        for (int i = 0; i < 7; i++) {
            LocalDate currentDay = startOfWeek.plusDays(i);
            Date start = Date.from(currentDay.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(currentDay.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());

            Double dayRev = tableOrderRepository.sumRevenueBetween(start, end);
            weeklyRevenueList.add(new DashboardStatsDTO.WeeklyRevenueDTO(dayNames[i], dayRev != null ? dayRev : 0.0));
        }

        // C. Biểu đồ tròn theo danh mục
        List<Object[]> rawCategorySales = tableOrderRepository.getSalesGroupedByCategory();
        List<DashboardStatsDTO.CategorySalesDTO> categorySalesList = rawCategorySales.stream()
                .map(row -> new DashboardStatsDTO.CategorySalesDTO((String) row[0], (Long) row[1]))
                .collect(Collectors.toList());

        return DashboardStatsDTO.builder()
                .todayRevenue(todayRevenue != null ? todayRevenue : 0.0)
                .todayOrderCount(todayOrderCount != null ? todayOrderCount : 0L)
                .monthRevenue(monthRevenue != null ? monthRevenue : 0.0)
                .weeklyRevenue(weeklyRevenueList)
                .categorySales(categorySalesList)
                .build();
    }
}