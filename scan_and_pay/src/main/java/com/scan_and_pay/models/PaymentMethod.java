package com.scan_and_pay.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_methods")
public class PaymentMethod {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "method_type", nullable = false, length = 50)
    private String methodType; // CARD, BANK, WALLET, UPI
    
    @Column(name = "provider_name")
    private String providerName; // VISA, MASTERCARD, PAYPAL, etc.
    
    @Column(name = "account_last_four")
    private String accountLastFour;
    
    @Column(columnDefinition = "TEXT")
    private String token; // Tokenized payment data
    
    @Column(name = "is_default")
    private boolean isDefault = false;
    
    @Column(name = "is_verified")
    private boolean isVerified = false;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    private String metadata; // Additional payment method details in JSON format
    
    // Constructors
    public PaymentMethod() {}
    
    public PaymentMethod(User user, String methodType, String token) {
        this.user = user;
        this.methodType = methodType;
        this.token = token;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public String getMethodType() { return methodType; }
    public void setMethodType(String methodType) { this.methodType = methodType; }
    
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    
    public String getAccountLastFour() { return accountLastFour; }
    public void setAccountLastFour(String accountLastFour) { this.accountLastFour = accountLastFour; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
    
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}