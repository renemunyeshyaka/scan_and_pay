package com.scan_and_pay.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dynamic_qr_codes")
public class DynamicQRCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;
    
    @Column(name = "qr_data", nullable = false, columnDefinition = "TEXT")
    private String qrData;
    
    @Column(precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "scanned_at")
    private LocalDateTime scannedAt;
    
    @Column(name = "scanned_by")
    private UUID scannedBy;
    
    private String purpose; // PAYMENT, DONATION, BILL_PAYMENT, etc.
    
    private String description;
    
    // Constructors
    public DynamicQRCode() {}
    
    public DynamicQRCode(Merchant merchant, String qrData, BigDecimal amount) {
        this.merchant = merchant;
        this.qrData = qrData;
        this.amount = amount;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Merchant getMerchant() { return merchant; }
    public void setMerchant(Merchant merchant) { this.merchant = merchant; }
    
    public Transaction getTransaction() { return transaction; }
    public void setTransaction(Transaction transaction) { this.transaction = transaction; }
    
    public String getQrData() { return qrData; }
    public void setQrData(String qrData) { this.qrData = qrData; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }
    
    public UUID getScannedBy() { return scannedBy; }
    public void setScannedBy(UUID scannedBy) { this.scannedBy = scannedBy; }
    
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}