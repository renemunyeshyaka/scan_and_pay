package com.scan_and_pay.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(length = 3)
    private String currency = "USD";
    
    @Column(length = 20)
    private String status = "PENDING"; // PENDING, SUCCESS, FAILED, CANCELLED, REFUNDED
    
    @Column(name = "qr_code_data")
    private String qrCodeData;
    
    @Column(name = "payment_link")
    private String paymentLink;
    
    @Column(name = "transaction_ref", unique = true, nullable = false)
    private String transactionRef;
    
    @Column(name = "payment_method")
    private String paymentMethod; // CARD, BANK, WALLET, UPI, etc.
    
    @Column(name = "payment_gateway_ref")
    private String paymentGatewayRef;
    
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "refund_amount", precision = 15, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;
    
    @Column(name = "is_refunded")
    private boolean isRefunded = false;
    
    // Constructors
    public Transaction() {}
    
    public Transaction(Merchant merchant, BigDecimal amount, String transactionRef) {
        this.merchant = merchant;
        this.amount = amount;
        this.transactionRef = transactionRef;
    }
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Merchant getMerchant() { return merchant; }
    public void setMerchant(Merchant merchant) { this.merchant = merchant; }
    
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
    
    public String getPaymentLink() { return paymentLink; }
    public void setPaymentLink(String paymentLink) { this.paymentLink = paymentLink; }
    
    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getPaymentGatewayRef() { return paymentGatewayRef; }
    public void setPaymentGatewayRef(String paymentGatewayRef) { this.paymentGatewayRef = paymentGatewayRef; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    
    public boolean isRefunded() { return isRefunded; }
    public void setRefunded(boolean refunded) { isRefunded = refunded; }
}