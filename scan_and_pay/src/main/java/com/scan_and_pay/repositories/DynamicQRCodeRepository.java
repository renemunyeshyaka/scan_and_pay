package com.scan_and_pay.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.scan_and_pay.models.DynamicQRCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DynamicQRCodeRepository extends JpaRepository<DynamicQRCode, UUID> {
    
    Optional<DynamicQRCode> findByQrData(String qrData);
    
    Optional<DynamicQRCode> findByQrDataAndIsActiveTrue(String qrData);
    
    List<DynamicQRCode> findByMerchantId(UUID merchantId);
    
    List<DynamicQRCode> findByMerchantIdAndIsActiveTrue(UUID merchantId);
    
    List<DynamicQRCode> findByIsActiveTrue();
    
    List<DynamicQRCode> findByIsActiveFalse();
    
    List<DynamicQRCode> findByPurpose(String purpose);
    
    List<DynamicQRCode> findByExpiresAtBefore(LocalDateTime dateTime);
    
    List<DynamicQRCode> findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime dateTime);
    
    List<DynamicQRCode> findByScannedBy(UUID scannedBy);
    
    List<DynamicQRCode> findByScannedByIsNotNull();
    
    List<DynamicQRCode> findByScannedByIsNull();
    
    List<DynamicQRCode> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT q FROM DynamicQRCode q WHERE q.merchant.id = :merchantId AND q.isActive = true AND q.expiresAt > CURRENT_TIMESTAMP")
    List<DynamicQRCode> findActiveValidQRCodesByMerchant(@Param("merchantId") UUID merchantId);
    
    @Query("SELECT COUNT(q) FROM DynamicQRCode q WHERE q.merchant.id = :merchantId AND q.isActive = true")
    Long countActiveQRCodesByMerchant(@Param("merchantId") UUID merchantId);
    
    @Query("SELECT q FROM DynamicQRCode q WHERE q.transaction IS NOT NULL")
    List<DynamicQRCode> findQRCodesWithTransaction();
    
    @Query("SELECT q FROM DynamicQRCode q WHERE q.transaction IS NULL AND q.isActive = true")
    List<DynamicQRCode> findUnusedActiveQRCodes();
    
    @Modifying
    @Transactional
    @Query("UPDATE DynamicQRCode q SET q.isActive = false WHERE q.expiresAt < :currentTime AND q.isActive = true")
    void deactivateExpiredQRCodes(@Param("currentTime") LocalDateTime currentTime);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM DynamicQRCode q WHERE q.expiresAt < :expiryTime AND q.isActive = false")
    void deleteExpiredInactiveQRCodes(@Param("expiryTime") LocalDateTime expiryTime);
    
    @Query("SELECT q.merchant.id, COUNT(q) FROM DynamicQRCode q WHERE q.isActive = true GROUP BY q.merchant.id")
    List<Object[]> countActiveQRCodesByMerchant();
    
    @Query("SELECT q.purpose, COUNT(q) FROM DynamicQRCode q GROUP BY q.purpose")
    List<Object[]> countQRCodesByPurpose();

    // Add these missing count methods
    @Query("SELECT COUNT(q) FROM DynamicQRCode q")
    Long countAllQRCodes();

    @Query("SELECT COUNT(q) FROM DynamicQRCode q WHERE q.isActive = true")
    Long countActiveQRCodes();

    @Query("SELECT COUNT(q) FROM DynamicQRCode q WHERE q.merchant.id = :merchantId")
    Long countByMerchantId(@Param("merchantId") UUID merchantId);
}