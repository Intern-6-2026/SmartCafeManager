package com.codegym.backend.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.codegym.backend.dto.EmployeeRequest;
import com.codegym.backend.dto.EmployeeResponse;
import com.codegym.backend.entity.Account;
import com.codegym.backend.entity.Employee;
import com.codegym.backend.entity.Role;
import com.codegym.backend.enums.AccountStatus;
import com.codegym.backend.repository.AccountRepository;
import com.codegym.backend.repository.EmployeeRepository;
import com.codegym.backend.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .filter(emp -> !emp.isDeleted() && !emp.getAccount().isDeleted())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public EmployeeResponse createEmployee(EmployeeRequest request, MultipartFile image) throws Exception {
        if (accountRepository.findByUsernameAndDeletedAtIsNull(request.getUsername()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (accountRepository.findByEmailAndDeletedAtIsNull(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại!");
        }

        Role staffRole = roleRepository.findByRoleName("STAFF")
                .orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Không tìm thấy quyền STAFF"));

        Account account = Account.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(staffRole)
                .status(AccountStatus.ACTIVE)
                .build();

        accountRepository.save(Objects.requireNonNull(account));

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(image);
        }
        Date dob = null;
        if (request.getDateOfBirth() != null && !request.getDateOfBirth().trim().isEmpty()) {
            dob = new SimpleDateFormat("yyyy-MM-dd").parse(request.getDateOfBirth());
        }
        Employee employee = Employee.builder()
                .account(account)
                .fullName(request.getFullName())
                .dateOfBirth(dob)
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .salary(request.getSalary())
                .imageUrl(imageUrl)
                .build();

        return mapToResponse(employeeRepository.save(Objects.requireNonNull(employee)));
    }

    @Transactional(rollbackFor = Exception.class)
    public EmployeeResponse updateEmployee(Long employeeId, EmployeeRequest request, MultipartFile image)
            throws Exception {

        Employee employee = employeeRepository.findById(Objects.requireNonNull(employeeId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));

        Account account = employee.getAccount();

        if (request.getEmail() != null && !request.getEmail().equals(account.getEmail())) {
            if (accountRepository.findByEmailAndDeletedAtIsNull(request.getEmail()).isPresent()) {
                throw new RuntimeException("Email đã được sử dụng bởi người khác!");
            }
            account.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
            account.setPasswordChangedAt(new Date());
        }
        Date dob = null;
        if (request.getDateOfBirth() != null && !request.getDateOfBirth().trim().isEmpty()) {
            dob = new SimpleDateFormat("yyyy-MM-dd").parse(request.getDateOfBirth());
        }

        employee.setDateOfBirth(dob);
        employee.setFullName(request.getFullName());
        employee.setGender(request.getGender());
        employee.setAddress(request.getAddress());
        employee.setSalary(request.getSalary());

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(employee.getPhoneNumber())) {
            if (employeeRepository.existsByPhoneNumberAndAccountNot(request.getPhoneNumber(), account)) {
                throw new RuntimeException("Số điện thoại đã tồn tại!");
            }
            employee.setPhoneNumber(request.getPhoneNumber());
        }

        if (image != null && !image.isEmpty()) {
            if (employee.getImageUrl() != null) {
                cloudinaryService.deleteImage(employee.getImageUrl());
            }
            employee.setImageUrl(cloudinaryService.uploadImage(image));
        }

        accountRepository.save(Objects.requireNonNull(account));
        return mapToResponse(employeeRepository.save(Objects.requireNonNull(employee)));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteEmployee(Long employeeId) {
        Employee employee = employeeRepository.findById(Objects.requireNonNull(employeeId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));

        Account account = employee.getAccount();

        Date now = new Date();

        account.setStatus(AccountStatus.INACTIVE);
        account.setDeletedAt(now);

        accountRepository.save(Objects.requireNonNull(account));

        employee.setDeletedAt(now);
        employeeRepository.save(Objects.requireNonNull(employee));
    }

    private EmployeeResponse mapToResponse(Employee emp) {
        return EmployeeResponse.builder()
                .employeeId(emp.getEmployeeId())
                .username(emp.getAccount().getUsername())
                .email(emp.getAccount().getEmail())
                .fullName(emp.getFullName())
                .dateOfBirth(emp.getDateOfBirth())
                .gender(emp.getGender() != null ? emp.getGender().name() : null)
                .phoneNumber(emp.getPhoneNumber())
                .address(emp.getAddress())
                .salary(emp.getSalary())
                .imageUrl(emp.getImageUrl())
                .status(emp.getAccount().getStatus().name())
                .build();
    }
}