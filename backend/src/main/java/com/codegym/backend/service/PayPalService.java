package com.codegym.backend.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Import thêm Locale

@Service
public class PayPalService {

    @Autowired
    private APIContext apiContext;

    // Tỷ giá quy đổi tạm thời VND -> USD để gửi qua PayPal
    private static final BigDecimal EXCHANGE_RATE_VND_TO_USD = new BigDecimal("25000");

    public String createPayPalOrder(BigDecimal totalAmountVnd, String returnUrl, String cancelUrl)
            throws PayPalRESTException {
        // Quy đổi VND sang USD (làm tròn 2 chữ số thập phân)
        BigDecimal totalAmountUsd = totalAmountVnd.divide(EXCHANGE_RATE_VND_TO_USD, 2, RoundingMode.HALF_UP);

        Amount amount = new Amount();
        amount.setCurrency("USD");
        // FIX LỖI Ở DÒNG NÀY: Dùng Locale.US để ép định dạng dấu chấm (.)
        amount.setTotal(String.format(Locale.US, "%.2f", totalAmountUsd));

        Transaction transaction = new Transaction();
        transaction.setDescription("Thanh toán hóa đơn nhà hàng SmartCafe");
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(returnUrl);
        payment.setRedirectUrls(redirectUrls);

        Payment createdPayment = payment.create(apiContext);

        for (Links link : createdPayment.getLinks()) {
            if (link.getRel().equalsIgnoreCase("approval_url")) {
                return link.getHref(); // Trả về link trang thanh toán của PayPal
            }
        }
        return null;
    }

    public boolean executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);

        Payment executedPayment = payment.execute(apiContext, paymentExecution);
        return "approved".equalsIgnoreCase(executedPayment.getState());
    }
}