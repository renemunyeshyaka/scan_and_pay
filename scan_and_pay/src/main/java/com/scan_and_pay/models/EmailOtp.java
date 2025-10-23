package com.scan_and_pay.models;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "email_otps")
@PrimaryKeyJoinColumn(name = "otp_id")
public class EmailOtp extends Otp {
    
    @Column(name = "email_subject")
    private String emailSubject;
    
    @Column(name = "email_body", columnDefinition = "TEXT")
    private String emailBody;
    
    @Column(name = "is_delivered")
    private boolean isDelivered = false;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "delivery_status")
    private String deliveryStatus = "PENDING"; // PENDING, SENT, FAILED
    
    // Constructors
    public EmailOtp() {}
    
    public EmailOtp(String code, String destination, String type, UUID userId) {
        super(code, destination, type, userId);
    }
    
    // Getters and Setters
    public String getEmailSubject() { return emailSubject; }
    public void setEmailSubject(String emailSubject) { this.emailSubject = emailSubject; }
    
    public String getEmailBody() { return emailBody; }
    public void setEmailBody(String emailBody) { this.emailBody = emailBody; }
    
    public boolean isDelivered() { return isDelivered; }
    public void setDelivered(boolean delivered) { isDelivered = delivered; }
    
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
}