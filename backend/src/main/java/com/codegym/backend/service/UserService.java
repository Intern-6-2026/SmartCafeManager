package com.codegym.backend.service;

import java.util.Date;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codegym.backend.DTO.*;
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

        Optional<Employee> empOpt = employeeRepository.findByAccount(account);
        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            emp.setFullName(request.getFullName());
            emp.setDateOfBirth(request.getDateOfBirth());
            emp.setGender(request.getGender());
            emp.setPhoneNumber(request.getPhoneNumber());
            emp.setAddress(request.getAddress());
            employeeRepository.save(emp);
            return getCurrentUserProfile();
        }

        Optional<Customer> cusOpt = customerRepository.findByAccount(account);
        if (cusOpt.isPresent()) {
            Customer cus = cusOpt.get();
            cus.setFullName(request.getFullName());
            cus.setDateOfBirth(request.getDateOfBirth());
            cus.setGender(request.getGender());
            cus.setPhoneNumber(request.getPhoneNumber());
            cus.setAddress(request.getAddress());
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