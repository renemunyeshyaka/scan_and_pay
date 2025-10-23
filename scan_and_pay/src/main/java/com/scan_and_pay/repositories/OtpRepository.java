package com.scan_and_pay.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.scan_and_pay.models.Otp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {
    
    Optional<Otp> findByCodeAndDestination(String code, String destination);
    
    List<Otp> findByUserId(UUID userId);
    
    List<Otp> findByDestination(String destination);
    
    List<Otp> findByType(String type);
    
    List<Otp> findByIsUsedTrue();
    
    List<Otp> findByIsUsedFalse();
    
    List<Otp> findByExpiresAtBefore(LocalDateTime dateTime);
    
    List<Otp> findByExpiresAtBeforeAndIsUsedFalse(LocalDateTime dateTime);
    
    @Query("SELECT o FROM Otp o WHERE o.userId = :userId AND o.type = :type AND o.isUsed = false ORDER BY o.createdAt DESC")
    Optional<Otp> findTopByUserIdAndTypeAndIsUsedFalseOrderByCreatedAtDesc(@Param("userId") UUID userId, 
                                                                         @Param("type") String type);
    
    @Query("SELECT o FROM Otp o WHERE o.userId = :userId AND o.type = :type AND o.isUsed = false")
    List<Otp> findByUserIdAndTypeAndIsUsedFalse(@Param("userId") UUID userId, 
                                              @Param("type") String type);
    
    @Query("SELECT o FROM Otp o WHERE o.destination = :destination AND o.type = :type AND o.isUsed = false AND o.expiresAt > CURRENT_TIMESTAMP")
    Optional<Otp> findValidOtpByDestinationAndType(@Param("destination") String destination, 
                                                 @Param("type") String type);
    
    @Query("SELECT COUNT(o) FROM Otp o WHERE o.userId = :userId AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Long countOtpsByUserAndDateRange(@Param("userId") UUID userId, 
                                   @Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Otp o WHERE o.expiresAt < :expiryTime AND o.isUsed = false")
    void deleteByExpiresAtBeforeAndIsUsedFalse(@Param("expiryTime") LocalDateTime expiryTime);
    
    @Modifying
    @Transactional
    @Query("UPDATE Otp o SET o.isUsed = true WHERE o.userId = :userId AND o.type = :type AND o.isUsed = false")
    void markAllByUserAndTypeAsUsed(@Param("userId") UUID userId, 
                                  @Param("type") String type);
    
    @Query("SELECT o.type, COUNT(o) FROM Otp o WHERE o.createdAt BETWEEN :startDate AND :endDate GROUP BY o.type")
    List<Object[]> countOtpsByTypeAndDateRange(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o.destination, COUNT(o) FROM Otp o WHERE o.isUsed = false AND o.expiresAt > CURRENT_TIMESTAMP GROUP BY o.destination HAVING COUNT(o) > :threshold")
    List<Object[]> findDestinationsWithExcessiveActiveOtps(@Param("threshold") Long threshold);
}