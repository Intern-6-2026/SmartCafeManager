package com.codegym.backend.service;

import java.util.Date;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codegym.backend.dto.*;
import com.codegym.backend.entity.Account;
import com.codegym.backend.entity.Customer;
import com.codegym.backend.entity.Employee;
import com.codegym.backend.repository.AccountRepository;
import com.codegym.backend.repository.CustomerRepository;
import com.codegym.backend.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getCurrentUserProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new RuntimeException("Account not found or has been deleted!"));
        String roleName = account.getRole() != null ? account.getRole().getRoleName() : "USER";

        Optional<Employee> empOpt = employeeRepository.findByAccount(account);
        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            return UserProfileResponse.builder()
                    .username(account.getUsername())
                    .email(account.getEmail())
                    .fullName(emp.getFullName())
                    .dateOfBirth(emp.getDateOfBirth())
                    .gender(emp.getGender() != null ? emp.getGender().name() : null)
                    .phone(emp.getPhoneNumber())
                    .address(emp.getAddress())
                    .salary(emp.getSalary())
                    .imageUrl(emp.getImageUrl())
                    .roleName(roleName)
                    .build();
        }

        Optional<Customer> cusOpt = customerRepository.findByAccount(account);
        if (cusOpt.isPresent()) {
            Customer cus = cusOpt.get();
            return UserProfileResponse.builder()
                    .username(account.getUsername())
                    .email(account.getEmail())
                    .fullName(cus.getFullName())
                    .dateOfBirth(cus.getDateOfBirth())
                    .gender(cus.getGender() != null ? cus.getGender().name() : null)
                    .phone(cus.getPhoneNumber())
                    .address(cus.getAddress())
                    .loyaltyPoints(cus.getLoyaltyPoints())
                    .imageUrl(cus.getImageUrl())
                    .roleName(roleName)
                    .build();
        }

        throw new RuntimeException("The account has not been set up with personal information (Profile)!");
    }

    @Transactional
    public UserProfileResponse updateProfile(UpdateProfileRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new RuntimeException("Account not found!"));

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String newEmail = request.getEmail().trim();

            if (!newEmail.equalsIgnoreCase(account.getEmail())) {
                Optional<Account> existingAccount = accountRepository.findByEmailAndDeletedAtIsNull(newEmail);
                if (existingAccount.isPresent()) {
                    throw new RuntimeException("Email này đã được đăng ký bởi một tài khoản khác trong hệ thống!");
                }
                account.setEmail(newEmail);
                accountRepository.save(account);
            }
        }

        Optional<Employee> empOpt = employeeRepository.findByAccount(account);
        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
                emp.setFullName(request.getFullName());
            }
            if (request.getDateOfBirth() != null)
                emp.setDateOfBirth(request.getDateOfBirth());
            if (request.getGender() != null)
                emp.setGender(request.getGender());
            if (request.getPhoneNumber() != null)
                emp.setPhoneNumber(request.getPhoneNumber());
            if (request.getAddress() != null)
                emp.setAddress(request.getAddress());
            if (request.getImageUrl() != null)
                emp.setImageUrl(request.getImageUrl());

            employeeRepository.save(emp);
            return getCurrentUserProfile();
        }

        Optional<Customer> cusOpt = customerRepository.findByAccount(account);
        if (cusOpt.isPresent()) {
            Customer cus = cusOpt.get();
            if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
                cus.setFullName(request.getFullName());
            }
            if (request.getDateOfBirth() != null)
                cus.setDateOfBirth(request.getDateOfBirth());
            if (request.getGender() != null)
                cus.setGender(request.getGender());
            if (request.getPhoneNumber() != null)
                cus.setPhoneNumber(request.getPhoneNumber());
            if (request.getAddress() != null)
                cus.setAddress(request.getAddress());
            if (request.getImageUrl() != null)
                cus.setImageUrl(request.getImageUrl());

            customerRepository.save(cus);
            return getCurrentUserProfile();
        }

        throw new RuntimeException("No personal profile was found to update!");
    }

    @Transactional
    public String changePassword(ChangePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountRepository.findByUsernameAndDeletedAtIsNull(username)
                .orElseThrow(() -> new RuntimeException("Account does not exist!"));
        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            throw new RuntimeException("Old password is incorrect!");
        }
        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new RuntimeException("New password cannot be the same as the old password!");
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        account.setPasswordChangedAt(new Date());
        accountRepository.save(account);

        return "Password changed successfully!";
    }
}