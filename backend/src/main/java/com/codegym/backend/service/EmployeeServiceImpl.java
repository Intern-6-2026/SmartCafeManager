package com.codegym.backend.service;

import java.security.SecureRandom;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.codegym.backend.enums.AccountStatus;
import com.codegym.backend.entity.Account;
import com.codegym.backend.dto.EmployeeCreateRequest;
import com.codegym.backend.dto.EmployeeResponse;
import com.codegym.backend.dto.UpdateProfileRequest;
import com.codegym.backend.entity.Employee;
import com.codegym.backend.entity.Role;
import com.codegym.backend.repository.AccountRepository;
import com.codegym.backend.repository.EmployeeRepository;
import com.codegym.backend.repository.RoleRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Override
    public Page<EmployeeResponse> getAllEmployees(int page, int size) {
        return employeeRepository.findByDeletedAtIsNull(PageRequest.of(page, size)).map(this::mapToResponse);
    }

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        Employee emp = employeeRepository.findById(id).filter(e -> !e.isDeleted())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên này"));
        return mapToResponse(emp);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if (accountRepository.existsByUsername(request.getUserName())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (employeeRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        if (request.getPhoneNumber() != null && employeeRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại đã tồn tại trên hệ thống");
        }

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò này"));

        String rawPassword = generateRandomPassword(8);

        Account newAccount = Account.builder()
                .username(request.getUserName())
                .password(passwordEncoder.encode(rawPassword))
                .email(request.getEmail())
                .role(role)
                .status(AccountStatus.ACTIVE)
                .passwordChangedAt(null)
                .build();
        accountRepository.save(newAccount);

        Employee newEmployee = Employee.builder()
                .account(newAccount)
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender() != null ? com.codegym.backend.enums.Gender.valueOf(request.getGender())
                        : null)
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .salary(request.getSalary())
                .build();
        employeeRepository.save(newEmployee);

        log.info("Đã tạo nhân viên mới: {}", newEmployee.getFullName());

        return mapToResponse(newEmployee);
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public EmployeeResponse updateEmployee(Long id, UpdateProfileRequest request) {
        Employee emp = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên này"));

        if (request.getEmail() != null && !request.getEmail().equals(emp.getAccount().getEmail())) {
            if (accountRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email đã tồn tại trên hệ thống");
            }
            emp.getAccount().setEmail(request.getEmail());
            accountRepository.save(emp.getAccount());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(emp.getPhoneNumber())) {
            if (employeeRepository.existsByPhoneNumberAndAccountNot(request.getPhoneNumber(), emp.getAccount())) {
                throw new RuntimeException("Số điện thoại đã tồn tại trên hệ thống");
            }
            emp.setPhoneNumber(request.getPhoneNumber());
        }

        emp.setFullName(request.getFullName());
        emp.setDateOfBirth(request.getDateOfBirth());
        emp.setGender(request.getGender());
        emp.setAddress(request.getAddress());

        return mapToResponse(employeeRepository.save(emp));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public EmployeeResponse updateEmployeeAvatar(Long id, MultipartFile imageUrl) {
        Employee emp = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên này"));

        String newImageUrl;
        try {
            newImageUrl = cloudinaryService.uploadImage(imageUrl);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Lỗi khi tải lên ảnh đại diện: " + e.getMessage());
        }

        if (newImageUrl == null) {
            throw new RuntimeException("Không thể tải lên ảnh đại diện");
        }
        emp.setImageUrl(newImageUrl);
        return mapToResponse(employeeRepository.save(emp));
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void deleteEmployee(Long id) {
        Employee emp = employeeRepository.findById(id)
                .filter(e -> !e.isDeleted())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên này"));

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (emp.getAccount().getUsername().equals(currentUsername)) {
            throw new RuntimeException("Không thể xóa chính mình");
        }

        emp.setDeletedAt(new Date());
        employeeRepository.save(emp);
        log.info("Đã xóa nhân viên: {}", emp.getFullName());

        Account account = emp.getAccount();
        account.setStatus(AccountStatus.INACTIVE);
        account.setDeletedAt(new Date());
        accountRepository.save(account);
    }

    private EmployeeResponse mapToResponse(Employee emp) {
        return EmployeeResponse.builder()
                .employeeId(emp.getEmployeeId())
                .userName(emp.getAccount().getUsername())
                .email(emp.getAccount().getEmail())
                .fullName(emp.getFullName())
                .dateOfBirth(emp.getDateOfBirth())
                .gender(emp.getGender() != null ? emp.getGender().name() : null)
                .phoneNumber(emp.getPhoneNumber())
                .address(emp.getAddress())
                .salary(emp.getSalary())
                .imageUrl(emp.getImageUrl())
                .roleName(emp.getAccount().getRole().getRoleName())
                .accountStatus(emp.getAccount().getStatus().name())
                .build();
    }

    private String generateRandomPassword(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        return sb.toString();
    }
}