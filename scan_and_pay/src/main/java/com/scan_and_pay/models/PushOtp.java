package com.scan_and_pay.models;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "push_otps")
@PrimaryKeyJoinColumn(name = "otp_id")
public class PushOtp extends Otp {
    
    @Column(name = "device_token")
    private String deviceToken;
    
    @Column(name = "push_title")
    private String pushTitle;
    
    @Column(name = "push_body")
    private String pushBody;
    
    @Column(name = "is_delivered")
    private boolean isDelivered = false;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(name = "device_type")
    private String deviceType; // ANDROID, IOS, WEB
    
    @Column(name = "delivery_status")
    private String deliveryStatus = "PENDING"; // PENDING, SENT, FAILED
    
    // Constructors
    public PushOtp() {}
    
    public PushOtp(String code, String destination, String type, UUID userId, String deviceToken) {
        super(code, destination, type, userId);
        this.deviceToken = deviceToken;
    }
    
    // Getters and Setters
    public String getDeviceToken() { return deviceToken; }
    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }
    
    public String getPushTitle() { return pushTitle; }
    public void setPushTitle(String pushTitle) { this.pushTitle = pushTitle; }
    
    public String getPushBody() { return pushBody; }
    public void setPushBody(String pushBody) { this.pushBody = pushBody; }
    
    public boolean isDelivered() { return isDelivered; }
    public void setDelivered(boolean delivered) { isDelivered = delivered; }
    
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }
}