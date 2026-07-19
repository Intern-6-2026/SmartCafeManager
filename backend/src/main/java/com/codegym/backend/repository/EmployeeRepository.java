package com.codegym.backend.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.codegym.backend.entity.Account;
import com.codegym.backend.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByAccount(Account account);

    boolean existsByPhoneNumberAndAccountNot(String phoneNumber, Account account);

    boolean existsByPhoneNumber(String phoneNumber);

    Page<Employee> findByDeletedAtIsNull(Pageable pageable);
}