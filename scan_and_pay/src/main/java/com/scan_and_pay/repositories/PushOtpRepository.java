package com.scan_and_pay.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.scan_and_pay.models.PushOtp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PushOtpRepository extends JpaRepository<PushOtp, UUID> {
    
    Optional<PushOtp> findByDeviceTokenAndCodeAndIsUsedFalse(String deviceToken, String code);
    
    List<PushOtp> findByDeviceToken(String deviceToken);
    
    List<PushOtp> findByDeviceType(String deviceType);
    
    List<PushOtp> findByDeliveryStatus(String deliveryStatus);
    
    List<PushOtp> findByIsDeliveredTrue();
    
    List<PushOtp> findByIsDeliveredFalse();
    
    List<PushOtp> findByExpiresAtBeforeAndIsUsedFalse(LocalDateTime dateTime);
    
    @Query("SELECT p FROM PushOtp p WHERE p.deviceToken = :deviceToken AND p.isUsed = false AND p.expiresAt > CURRENT_TIMESTAMP ORDER BY p.createdAt DESC")
    Optional<PushOtp> findLatestValidPushOtp(@Param("deviceToken") String deviceToken);
    
    @Query("SELECT p FROM PushOtp p WHERE p.userId = :userId AND p.deviceType = :deviceType AND p.isUsed = false")
    List<PushOtp> findActivePushOtpsByUserAndDeviceType(@Param("userId") UUID userId, 
                                                      @Param("deviceType") String deviceType);
    
    @Query("SELECT COUNT(p) FROM PushOtp p WHERE p.deviceToken = :deviceToken AND p.createdAt >= :startDate")
    Long countPushOtpsSentToday(@Param("deviceToken") String deviceToken, 
                              @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT p.deviceType, COUNT(p) FROM PushOtp p WHERE p.deliveryStatus = 'SENT' GROUP BY p.deviceType")
    List<Object[]> countSentPushOtpsByDeviceType();
    
    @Query("SELECT p FROM PushOtp p WHERE p.deliveryStatus = 'FAILED' AND p.createdAt >= :sinceDate")
    List<PushOtp> findFailedPushOtpsSince(@Param("sinceDate") LocalDateTime sinceDate);
}