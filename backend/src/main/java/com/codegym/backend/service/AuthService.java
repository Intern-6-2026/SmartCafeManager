package com.codegym.backend.service;

import java.security.SecureRandom;
import java.util.Date;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại hoặc đã bị xóa!"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản của bạn chưa được kích hoạt hoặc đang bị khóa!");
        }

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new RuntimeException("Mật khẩu không chính xác!");
        }

        if (account.getRole() == null || account.getRole().getRoleName() == null) {
            throw new RuntimeException("Tài khoản chưa được phân quyền trên hệ thống");
        }
        String roleName = account.getRole().getRoleName();

        String userName = account.getUsername();

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

        return new LoginResponse(token, "Đăng nhập thành công!", requirePasswordChange, roleName, userName);
    }

    @Transactional
    public String processForgotPassword(ForgotPasswordRequest request) {
        Account account = accountRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản hợp lệ với email này!"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Tài khoản đang bị khóa hoặc không hoạt động, không thể khôi phục mật khẩu!");
        }

        SecureRandom random = new SecureRandom();
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);

        account.setResetToken(otp);
        account.setResetTokenExpiry(new Date(System.currentTimeMillis() + 5 * 60 * 1000));

        accountRepository.save(account);

        emailService.sendPasswordResetMail(account.getEmail(), otp);

        return "Mã OTP khôi phục mật khẩu đã được gửi đến email của bạn.";
    }

    @Transactional
    public String processResetPassword(ResetPasswordRequest request) {
        Account account = accountRepository.findByResetTokenAndDeletedAtIsNull(request.getToken())
                .orElseThrow(() -> new RuntimeException("Mã OTP không hợp lệ."));

        if (account.getResetTokenExpiry().before(new Date())) {
            throw new RuntimeException("Mã khôi phục đã hết hạn (quá 5 phút)!");
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        account.setPasswordChangedAt(new Date());
        account.setResetToken(null);
        account.setResetTokenExpiry(null);

        accountRepository.save(account);

        return "Cập nhật mật khẩu mới thành công!";
    }
}