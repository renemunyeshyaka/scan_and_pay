package com.scan_and_pay.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.scan_and_pay.models.Admin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {
    
    Optional<Admin> findByEmail(String email);
    
    List<Admin> findByAdminLevel(String adminLevel);
    
    List<Admin> findByDepartment(String department);
    
    List<Admin> findByCanManageUsersTrue();
    
    List<Admin> findByCanManageTransactionsTrue();
    
    List<Admin> findByCanManageMerchantsTrue();
    
    @Query("SELECT a FROM Admin a WHERE a.adminLevel = 'SUPER_ADMIN'")
    List<Admin> findAllSuperAdmins();
    
    @Query("SELECT a FROM Admin a WHERE a.department = :department AND a.canManageUsers = true")
    List<Admin> findUserManagersByDepartment(@Param("department") String department);
    
    @Query("SELECT COUNT(a) FROM Admin a WHERE a.enabled = true")
    Long countActiveAdmins();
}