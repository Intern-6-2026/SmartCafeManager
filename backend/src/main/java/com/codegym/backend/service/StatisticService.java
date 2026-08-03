package com.codegym.backend.service;

import com.codegym.backend.dto.DashboardStatsDTO;
import com.codegym.backend.dto.InvoiceResponseDTO;

import java.util.List;

public interface StatisticService {
    List<InvoiceResponseDTO> getInvoices(Long tableId);
    DashboardStatsDTO getDashboardStats();
}