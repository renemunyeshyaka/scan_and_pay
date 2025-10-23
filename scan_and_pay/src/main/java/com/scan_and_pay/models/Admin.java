package com.scan_and_pay.models;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
@PrimaryKeyJoinColumn(name = "user_id")
public class Admin extends User {
    
    @Column(name = "admin_level")
    private String adminLevel = "SUPER_ADMIN"; // SUPER_ADMIN, SUPPORT_ADMIN, FINANCE_ADMIN
    
    @Column(name = "department")
    private String department;
    
    @Column(name = "can_manage_users")
    private boolean canManageUsers = true;
    
    @Column(name = "can_manage_transactions")
    private boolean canManageTransactions = true;
    
    @Column(name = "can_manage_merchants")
    private boolean canManageMerchants = true;
    
    // Constructors
    public Admin() {
        super();
        this.setRole("ADMIN");
    }
    
    public Admin(String email, String name, String password, String adminLevel) {
        super(email, name, password, "ADMIN");
        this.adminLevel = adminLevel;
    }
    
    // Getters and Setters
    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public boolean isCanManageUsers() { return canManageUsers; }
    public void setCanManageUsers(boolean canManageUsers) { this.canManageUsers = canManageUsers; }
    
    public boolean isCanManageTransactions() { return canManageTransactions; }
    public void setCanManageTransactions(boolean canManageTransactions) { 
        this.canManageTransactions = canManageTransactions; 
    }
    
    public boolean isCanManageMerchants() { return canManageMerchants; }
    public void setCanManageMerchants(boolean canManageMerchants) { 
        this.canManageMerchants = canManageMerchants; 
    }
}