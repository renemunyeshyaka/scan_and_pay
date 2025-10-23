package com.scan_and_pay.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.scan_and_pay.models.EmailOtp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailOtpRepository extends JpaRepository<EmailOtp, UUID> {
    
    Optional<EmailOtp> findByDestinationAndCodeAndIsUsedFalse(String destination, String code);
    
    List<EmailOtp> findByDestination(String destination);
    
    List<EmailOtp> findByDeliveryStatus(String deliveryStatus);
    
    List<EmailOtp> findByIsDeliveredTrue();
    
    List<EmailOtp> findByIsDeliveredFalse();
    
    List<EmailOtp> findByExpiresAtBeforeAndIsUsedFalse(LocalDateTime dateTime);
    
    @Query("SELECT e FROM EmailOtp e WHERE e.destination = :email AND e.isUsed = false AND e.expiresAt > CURRENT_TIMESTAMP ORDER BY e.createdAt DESC")
    Optional<EmailOtp> findLatestValidEmailOtp(@Param("email") String email);
    
    @Query("SELECT COUNT(e) FROM EmailOtp e WHERE e.destination = :email AND e.createdAt >= :startDate")
    Long countEmailOtpsSentToday(@Param("email") String email, 
                               @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT e FROM EmailOtp e WHERE e.deliveryStatus = 'FAILED' AND e.createdAt >= :sinceDate")
    List<EmailOtp> findFailedEmailOtpsSince(@Param("sinceDate") LocalDateTime sinceDate);
    
    @Query("SELECT e.destination, COUNT(e) FROM EmailOtp e WHERE e.deliveryStatus = 'FAILED' AND e.createdAt >= :sinceDate GROUP BY e.destination")
    List<Object[]> countFailedEmailOtpsByDestination(@Param("sinceDate") LocalDateTime sinceDate);
}