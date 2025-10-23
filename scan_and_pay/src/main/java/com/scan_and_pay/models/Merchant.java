package com.scan_and_pay.models;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "merchants")
@PrimaryKeyJoinColumn(name = "user_id")
public class Merchant extends User {
    
    @Column(name = "business_name", nullable = false)
    private String businessName;
    
    @Column(name = "business_registration_number", unique = true)
    private String businessRegistrationNumber;
    
    @Column(name = "tax_id")
    private String taxId;
    
    @Column(name = "wallet_balance", precision = 15, scale = 2)
    private BigDecimal walletBalance = BigDecimal.ZERO;
    
    @Column(name = "is_verified")
    private boolean isVerified = false;
    
    @Column(name = "is_active")
    private boolean isActive = true;
    
    private String address;
    
    private String city;
    
    private String country;
    
    // Constructors
    public Merchant() {
        super();
        this.setRole("MERCHANT");
    }
    
    public Merchant(String email, String name, String password, String businessName) {
        super(email, name, password, "MERCHANT");
        this.businessName = businessName;
    }
    
    // Getters and Setters
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    
    public String getBusinessRegistrationNumber() { return businessRegistrationNumber; }
    public void setBusinessRegistrationNumber(String businessRegistrationNumber) { 
        this.businessRegistrationNumber = businessRegistrationNumber; 
    }
    
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    
    public BigDecimal getWalletBalance() { return walletBalance; }
    public void setWalletBalance(BigDecimal walletBalance) { this.walletBalance = walletBalance; }
    
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}