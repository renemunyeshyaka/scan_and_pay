package com.scan_and_pay.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.scan_and_pay.models.Merchant;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    
    Optional<Merchant> findByBusinessName(String businessName);
    
    Optional<Merchant> findByBusinessRegistrationNumber(String businessRegistrationNumber);
    
    Optional<Merchant> findByTaxId(String taxId);
    
    List<Merchant> findByIsVerifiedTrue();
    
    List<Merchant> findByIsVerifiedFalse();
    
    List<Merchant> findByIsActiveTrue();
    
    List<Merchant> findByIsActiveFalse();
    
    List<Merchant> findByCity(String city);
    
    List<Merchant> findByCountry(String country);
    
    @Query("SELECT m FROM Merchant m WHERE m.walletBalance >= :minBalance")
    List<Merchant> findByWalletBalanceGreaterThanEqual(@Param("minBalance") BigDecimal minBalance);
    
    @Query("SELECT m FROM Merchant m WHERE m.walletBalance <= :maxBalance")
    List<Merchant> findByWalletBalanceLessThanEqual(@Param("maxBalance") BigDecimal maxBalance);
    
    @Query("SELECT COUNT(m) FROM Merchant m WHERE m.isVerified = true")
    Long countVerifiedMerchants();
    
    @Query("SELECT SUM(m.walletBalance) FROM Merchant m")
    BigDecimal getTotalWalletBalance();
    
    @Modifying
    @Transactional
    @Query("UPDATE Merchant m SET m.walletBalance = m.walletBalance + :amount WHERE m.id = :merchantId")
    void addToWalletBalance(@Param("merchantId") UUID merchantId, @Param("amount") BigDecimal amount);
    
    @Modifying
    @Transactional
    @Query("UPDATE Merchant m SET m.walletBalance = m.walletBalance - :amount WHERE m.id = :merchantId AND m.walletBalance >= :amount")
    int deductFromWalletBalance(@Param("merchantId") UUID merchantId, @Param("amount") BigDecimal amount);
    
    @Query("SELECT m FROM Merchant m WHERE m.businessName LIKE %:businessName%")
    List<Merchant> findByBusinessNameContaining(@Param("businessName") String businessName);
    
    @Query("SELECT m FROM Merchant m WHERE m.createdAt >= CURRENT_DATE")
    List<Merchant> findMerchantsRegisteredToday();
}