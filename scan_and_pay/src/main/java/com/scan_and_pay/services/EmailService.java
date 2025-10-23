package com.scan_and_pay.services;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public boolean sendOTPEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            // Log the error
            System.err.println("Failed to send email: " + e.getMessage());
            return false;
        }
    }

    public boolean sendPaymentSuccessEmail(String toEmail, String merchantName, BigDecimal amount) {
        String subject = "Payment Successful";
        String body = String.format(
            "Your payment of %s to %s was successful. Thank you for using ScanPay!",
            amount.toString(), merchantName
        );
        
        return sendOTPEmail(toEmail, subject, body);
    }

    // Add more email methods as needed
}