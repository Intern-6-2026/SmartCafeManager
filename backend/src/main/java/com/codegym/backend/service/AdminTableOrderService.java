package com.codegym.backend.service;

import com.codegym.backend.dto.AdminTableOrderRequestDTO;
import com.codegym.backend.entity.TableOrder;
import com.codegym.backend.enums.StatusTableOrder;
import com.codegym.backend.repository.CustomerRepository;
import com.codegym.backend.repository.EmployeeRepository;
import com.codegym.backend.repository.TableOrderRepository;
import com.codegym.backend.repository.TablesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("all")// THÊM DÒNG NÀY ĐỂ TẮT CẢNH BÁO VÀNG CỦA VS CODE
public class AdminTableOrderService {

    private final TableOrderRepository tableOrderRepository;
    private final TablesRepository tablesRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;

    public List<TableOrder> getAllOrders() {
        return tableOrderRepository.findAllByOrderByTableOrderIdDesc();
    }

    @Transactional
    public TableOrder updateOrder(Long orderId, AdminTableOrderRequestDTO dto) {
        if (orderId == null) {
            throw new IllegalArgumentException("Mã hóa đơn không được để trống");
        }

        TableOrder order = tableOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));

        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        order.setUpdatedBy(adminUsername);

        if (dto.getTotalAmount() != null) order.setTotalAmount(dto.getTotalAmount());
        if (dto.getPaymentMethod() != null) order.setPaymentMethod(dto.getPaymentMethod());
        if (dto.getStatus() != null) {
            order.setStatus(dto.getStatus());
            if (dto.getStatus() == StatusTableOrder.PAID && order.getPaidAt() == null) {
                order.setPaidAt(LocalDateTime.now());
                order.setCloseAt(LocalDateTime.now());
            }
        }

        Long tableId = dto.getTableId();
        if (tableId != null) {
            tablesRepository.findById(tableId).ifPresent(order::setTable);
        }

        Long customerId = dto.getCustomerId();
        if (customerId != null) {
            customerRepository.findById(customerId).ifPresent(order::setCustomer);
        }

        Long employeeId = dto.getEmployeeId();
        if (employeeId != null) {
            employeeRepository.findById(employeeId).ifPresent(order::setEmployee);
        }

        return tableOrderRepository.save(order);
    }

    @Transactional
    public void softDeleteOrder(Long orderId, String reason) {
        if (orderId == null) {
            throw new IllegalArgumentException("Mã hóa đơn không được để trống");
        }

        TableOrder order = tableOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));

        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        order.setIsDeleted(true);
        order.setCancelReason(reason);
        order.setUpdatedBy(adminUsername);
        order.setStatus(StatusTableOrder.CANCELLED);
        order.setCloseAt(LocalDateTime.now());

        tableOrderRepository.save(order);
    }
}