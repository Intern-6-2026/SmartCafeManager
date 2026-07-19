package com.codegym.backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.codegym.backend.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsernameAndDeletedAtIsNull(String username);

    Optional<Account> findByEmailAndDeletedAtIsNull(String email);

    Optional<Account> findByResetTokenAndDeletedAtIsNull(String resetToken);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}