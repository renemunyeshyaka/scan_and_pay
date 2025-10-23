package com.scan_and_pay.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.scan_and_pay.models.Transaction;

@Service
public class NotificationService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private PushNotificationService pushNotificationService;

    public void sendPaymentSuccessNotification(Transaction transaction) {
        // Send email notification
        if (transaction.getCustomer() != null && transaction.getCustomer().isEmailVerified()) {
            emailService.sendPaymentSuccessEmail(
                transaction.getCustomer().getEmail(),
                transaction.getMerchant().getBusinessName(),
                transaction.getAmount()
            );
        }

        // Send push notification
        // pushNotificationService.sendPaymentSuccessPush(transaction);
    }

    public void sendPaymentFailedNotification(Transaction transaction) {
        // Implement failed payment notifications
    }

    public void sendRefundNotification(Transaction transaction) {
        // Implement refund notifications
    }
}