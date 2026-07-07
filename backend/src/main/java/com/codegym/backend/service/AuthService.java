package com.codegym.backend.service;

import java.security.SecureRandom;
import java.util.Date;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.codegym.backend.dto.ForgotPasswordRequest;
import com.codegym.backend.dto.LoginRequest;
import com.codegym.backend.dto.LoginResponse;
import com.codegym.backend.dto.ResetPasswordRequest;
import com.codegym.backend.entity.Account;
import com.codegym.backend.enums.AccountStatus;
import com.codegym.backend.repository.AccountRepository;
import com.codegym.backend.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public LoginResponse login(LoginRequest request) {
        Account account = accountRepository.findByUsernameAndDeletedAtIsNull(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Account does not exist or has been deleted!"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Your account is not activated or is temporarily locked!");
        }

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new RuntimeException("Incorrect password!");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userDetails);

        long thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000;
        boolean requirePasswordChange = false;

        if (account.getPasswordChangedAt() == null ||
                (System.currentTimeMillis() - account.getPasswordChangedAt().getTime() > thirtyDaysInMillis)) {
            requirePasswordChange = true;
        }

        return new LoginResponse(token, "Login successful!", requirePasswordChange);
    }

    public String processForgotPassword(ForgotPasswordRequest request) {
        Account account = accountRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No valid account found for this email!"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("The account is locked or inactive and cannot recover the password!");
        }

        SecureRandom random = new SecureRandom();
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);

        account.setResetToken(otp);
        account.setResetTokenExpiry(new Date(System.currentTimeMillis() + 5 * 60 * 1000));

        accountRepository.save(account);
        emailService.sendPasswordResetMail(account.getEmail(), otp);

        return "Password recovery OTP has been sent to your email.";
    }

    public String processResetPassword(ResetPasswordRequest request) {
        Account account = accountRepository.findByResetTokenAndDeletedAtIsNull(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid recovery token or account does not exist!"));

        if (account.getResetTokenExpiry().before(new Date())) {
            throw new RuntimeException("Recovery token has expired (over 5 minutes)!");
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        account.setPasswordChangedAt(new Date());
        account.setResetToken(null);
        account.setResetTokenExpiry(null);

        accountRepository.save(account);

        return "New password updated successfully!";
    }
}