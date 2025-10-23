package com.scan_and_pay.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.scan_and_pay.models.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    Optional<Transaction> findByTransactionRef(String transactionRef);
    
    List<Transaction> findByMerchantId(UUID merchantId);
    
    List<Transaction> findByCustomerId(UUID customerId);
    
    List<Transaction> findByStatus(String status);
    
    List<Transaction> findByPaymentMethod(String paymentMethod);
    
    List<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Transaction> findByCompletedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Transaction> findByStatusAndCreatedAtBefore(String status, LocalDateTime date);
    
    List<Transaction> findByIsRefundedTrue();
    
    List<Transaction> findByIsRefundedFalse();
    
    // Pagination support
    Page<Transaction> findByMerchantId(UUID merchantId, Pageable pageable);
    
    Page<Transaction> findByCustomerId(UUID customerId, Pageable pageable);
    
    Page<Transaction> findByStatus(String status, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.merchant.id = :merchantId AND t.status = 'SUCCESS'")
    List<Transaction> findSuccessfulTransactionsByMerchant(@Param("merchantId") UUID merchantId);
    
    @Query("SELECT t FROM Transaction t WHERE t.customer.id = :customerId AND t.status = 'SUCCESS'")
    List<Transaction> findSuccessfulTransactionsByCustomer(@Param("customerId") UUID customerId);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.merchant.id = :merchantId AND t.status = 'SUCCESS'")
    Long countSuccessfulTransactionsByMerchant(@Param("merchantId") UUID merchantId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.merchant.id = :merchantId AND t.status = 'SUCCESS'")
    BigDecimal sumAmountByMerchant(@Param("merchantId") UUID merchantId);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.merchant.id = :merchantId AND t.status = 'SUCCESS' AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByMerchantAndDateRange(@Param("merchantId") UUID merchantId, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = 'SUCCESS' AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSalesByDateRange(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = 'SUCCESS' AND t.createdAt BETWEEN :startDate AND :endDate")
    Long countSuccessfulTransactionsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.amount >= :minAmount AND t.amount <= :maxAmount")
    List<Transaction> findByAmountBetween(@Param("minAmount") BigDecimal minAmount, 
                                        @Param("maxAmount") BigDecimal maxAmount);
    
    @Query("SELECT t FROM Transaction t WHERE t.merchant.id = :merchantId AND t.createdAt >= :startDate ORDER BY t.createdAt DESC")
    List<Transaction> findRecentTransactionsByMerchant(@Param("merchantId") UUID merchantId, 
                                                     @Param("startDate") LocalDateTime startDate);
    
    @Modifying
    @Transactional
    @Query("UPDATE Transaction t SET t.status = 'EXPIRED' WHERE t.status = 'PENDING' AND t.createdAt < :expiryTime")
    int expirePendingTransactions(@Param("expiryTime") LocalDateTime expiryTime);
    
    @Query("SELECT t.merchant.id, COUNT(t) FROM Transaction t WHERE t.status = 'SUCCESS' GROUP BY t.merchant.id ORDER BY COUNT(t) DESC")
    List<Object[]> findTransactionCountByMerchant();
    
    @Query("SELECT t.merchant.id, SUM(t.amount) FROM Transaction t WHERE t.status = 'SUCCESS' GROUP BY t.merchant.id ORDER BY SUM(t.amount) DESC")
    List<Object[]> findTransactionVolumeByMerchant();
    
    @Query("SELECT DATE(t.createdAt), COUNT(t), SUM(t.amount) FROM Transaction t WHERE t.status = 'SUCCESS' AND t.createdAt BETWEEN :startDate AND :endDate GROUP BY DATE(t.createdAt) ORDER BY DATE(t.createdAt)")
    List<Object[]> getDailyTransactionStats(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);

    // Count transactions by status
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    Long countByStatus(@Param("status") String status);

    // Find transactions with pagination and filtering
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:merchantId IS NULL OR t.merchant.id = :merchantId) AND " +
           "(:customerId IS NULL OR t.customer.id = :customerId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR t.createdAt <= :endDate)")
    Page<Transaction> findWithFilters(@Param("merchantId") UUID merchantId,
                                    @Param("customerId") UUID customerId,
                                    @Param("status") String status,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    Pageable pageable);
}