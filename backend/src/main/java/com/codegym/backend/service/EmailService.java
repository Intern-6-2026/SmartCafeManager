package com.codegym.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendPasswordResetMail(String to, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Recovery Request - Smart Cafe");
        message.setText("Hello,\n\n"
                + "You have requested to reset your password. Please use the code (token) below to set a new password:\n\n"
                + token + "\n\n"
                + "This code will expire within 5 minutes.\n"
                + "If you did not request this, please ignore this email.");

        mailSender.send(message);
    }
}