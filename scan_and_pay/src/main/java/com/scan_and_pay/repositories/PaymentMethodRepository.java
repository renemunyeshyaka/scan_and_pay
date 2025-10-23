package com.scan_and_pay.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.scan_and_pay.models.PaymentMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    
    List<PaymentMethod> findByUserId(UUID userId);
    
    List<PaymentMethod> findByUserIdAndIsActiveTrue(UUID userId);
    
    Optional<PaymentMethod> findByUserIdAndIsDefaultTrueAndIsActiveTrue(UUID userId);
    
    List<PaymentMethod> findByMethodType(String methodType);
    
    List<PaymentMethod> findByProviderName(String providerName);
    
    List<PaymentMethod> findByIsVerifiedTrue();
    
    List<PaymentMethod> findByIsVerifiedFalse();
    
    List<PaymentMethod> findByIsActiveTrue();
    
    List<PaymentMethod> findByIsActiveFalse();
    
    List<PaymentMethod> findByExpiresAtBefore(LocalDateTime dateTime);
    
    List<PaymentMethod> findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime dateTime);
    
    @Query("SELECT p FROM PaymentMethod p WHERE p.user.id = :userId AND p.methodType = :methodType AND p.isActive = true")
    List<PaymentMethod> findByUserIdAndMethodType(@Param("userId") UUID userId, 
                                                @Param("methodType") String methodType);
    
    @Query("SELECT p FROM PaymentMethod p WHERE p.user.id = :userId AND p.isDefault = true")
    Optional<PaymentMethod> findDefaultByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(p) FROM PaymentMethod p WHERE p.user.id = :userId AND p.isActive = true")
    Long countActivePaymentMethodsByUser(@Param("userId") UUID userId);
    
    @Query("SELECT p.methodType, COUNT(p) FROM PaymentMethod p WHERE p.isActive = true GROUP BY p.methodType")
    List<Object[]> countPaymentMethodsByType();
    
    @Modifying
    @Transactional
    @Query("UPDATE PaymentMethod p SET p.isActive = false WHERE p.expiresAt < :currentTime AND p.isActive = true")
    void deactivateExpiredPaymentMethods(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT p FROM PaymentMethod p WHERE p.user.id = :userId AND p.isVerified = true AND p.isActive = true ORDER BY p.isDefault DESC, p.createdAt DESC")
    List<PaymentMethod> findVerifiedActivePaymentMethodsByUser(@Param("userId") UUID userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE PaymentMethod p SET p.isDefault = false WHERE p.user.id = :userId AND p.isDefault = true")
    void unsetAllDefaultPaymentMethods(@Param("userId") UUID userId);
    
    @Query("SELECT p FROM PaymentMethod p WHERE p.accountLastFour = :lastFour AND p.isActive = true")
    List<PaymentMethod> findByAccountLastFour(@Param("lastFour") String lastFour);
}