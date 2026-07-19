package com.codegym.backend.service;

import com.codegym.backend.dto.EmployeeResponse;
import com.codegym.backend.dto.EmployeeCreateRequest;
import com.codegym.backend.dto.UpdateProfileRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;

public interface EmployeeService {
    Page<EmployeeResponse> getAllEmployees(int page, int size);

    EmployeeResponse getEmployeeById(Long id);

    EmployeeResponse createEmployee(EmployeeCreateRequest request);

    EmployeeResponse updateEmployee(Long id, UpdateProfileRequest request);

    EmployeeResponse updateEmployeeAvatar(Long id, MultipartFile avatarFile) throws Exception;

    void deleteEmployee(Long id);
}
