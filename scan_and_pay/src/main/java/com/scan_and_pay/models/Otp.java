package com.scan_and_pay.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otps")
@Inheritance(strategy = InheritanceType.JOINED)
public class Otp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 10)
    private String code;
    
    @Column(nullable = false)
    private String destination; // email or phone number
    
    @Column(nullable = false, length = 20)
    private String type; // LOGIN, REGISTRATION, PAYMENT, PASSWORD_RESET
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "is_used")
    private boolean isUsed = false;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    private int attempts = 0;
    
    @Column(name = "max_attempts")
    private int maxAttempts = 3;
    
    // Constructors
    public Otp() {}
    
    public Otp(String code, String destination, String type, UUID userId) {
        this.code = code;
        this.destination = destination;
        this.type = type;
        this.userId = userId;
        this.expiresAt = LocalDateTime.now().plusMinutes(10); // Default 10 minutes expiry
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public boolean isUsed() { return isUsed; }
    public void setUsed(boolean used) { isUsed = used; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    
    // Utility methods
    public boolean isValid() {
        return !isUsed && attempts < maxAttempts && LocalDateTime.now().isBefore(expiresAt);
    }
    
    public void incrementAttempts() {
        this.attempts++;
    }
}