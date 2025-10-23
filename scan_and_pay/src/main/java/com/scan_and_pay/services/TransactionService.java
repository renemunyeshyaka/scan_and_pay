package com.scan_and_pay.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scan_and_pay.models.Merchant;
import com.scan_and_pay.models.Transaction;
import com.scan_and_pay.models.User;
import com.scan_and_pay.repositories.MerchantRepository;
import com.scan_and_pay.repositories.TransactionRepository;
import com.scan_and_pay.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private NotificationService notificationService;

    // Get all transactions
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    // Get all transactions with pagination
    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    public Transaction createTransaction(UUID merchantId, BigDecimal amount, String description) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new RuntimeException("Merchant not found with id: " + merchantId));

        String transactionRef = generateTransactionReference();
        
        Transaction transaction = new Transaction();
        transaction.setMerchant(merchant);
        transaction.setAmount(amount);
        transaction.setTransactionRef(transactionRef);
        transaction.setDescription(description);
        transaction.setStatus("PENDING");

        // Generate QR code data
        String qrData = qrCodeService.generateQRCodeData(transaction);
        transaction.setQrCodeData(qrData);

        return transactionRepository.save(transaction);
    }

    public Optional<Transaction> getTransactionById(UUID id) {
        return transactionRepository.findById(id);
    }

    public Optional<Transaction> getTransactionByReference(String transactionRef) {
        return transactionRepository.findByTransactionRef(transactionRef);
    }

    public List<Transaction> getTransactionsByMerchant(UUID merchantId) {
        return transactionRepository.findByMerchantId(merchantId);
    }

    public List<Transaction> getTransactionsByCustomer(UUID customerId) {
        return transactionRepository.findByCustomerId(customerId);
    }

    public List<Transaction> getTransactionsByStatus(String status) {
        return transactionRepository.findByStatus(status);
    }

    public Transaction processPayment(String transactionRef, UUID customerId, String paymentMethod) {
        Transaction transaction = transactionRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!"PENDING".equals(transaction.getStatus())) {
            throw new RuntimeException("Transaction already processed");
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Process payment (integrate with payment gateway here)
        boolean paymentSuccess = processPaymentWithGateway(transaction, paymentMethod);

        if (paymentSuccess) {
            transaction.setStatus("SUCCESS");
            transaction.setCustomer(customer);
            transaction.setPaymentMethod(paymentMethod);
            transaction.setCompletedAt(LocalDateTime.now());
            transaction.setPaymentGatewayRef(generatePaymentGatewayRef());

            // Update merchant wallet
            updateMerchantWallet(transaction.getMerchant(), transaction.getAmount());

            // Send notifications
            notificationService.sendPaymentSuccessNotification(transaction);
        } else {
            transaction.setStatus("FAILED");
            transaction.setCompletedAt(LocalDateTime.now());
            notificationService.sendPaymentFailedNotification(transaction);
        }

        return transactionRepository.save(transaction);
    }

    public Transaction refundTransaction(UUID transactionId, BigDecimal refundAmount) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!"SUCCESS".equals(transaction.getStatus())) {
            throw new RuntimeException("Only successful transactions can be refunded");
        }

        if (refundAmount.compareTo(transaction.getAmount()) > 0) {
            throw new RuntimeException("Refund amount cannot exceed original amount");
        }

        // Process refund with payment gateway
        boolean refundSuccess = processRefundWithGateway(transaction, refundAmount);

        if (refundSuccess) {
            transaction.setRefundAmount(refundAmount);
            transaction.setRefunded(true);
            transaction.setStatus("REFUNDED");

            // Deduct from merchant wallet
            deductFromMerchantWallet(transaction.getMerchant(), refundAmount);

            notificationService.sendRefundNotification(transaction);
        }

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByCreatedAtBetween(startDate, endDate);
    }

    public BigDecimal getTotalSalesByMerchant(UUID merchantId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.sumAmountByMerchantAndDateRange(merchantId, startDate, endDate);
    }

    // Count methods for dashboard
    public Long countAllTransactions() {
        return transactionRepository.count();
    }

    public Long countSuccessfulTransactionsByMerchant(UUID merchantId) {
        return transactionRepository.countSuccessfulTransactionsByMerchant(merchantId);
    }

    public Long countSuccessfulTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.countSuccessfulTransactionsByDateRange(startDate, endDate);
    }

    public BigDecimal getTotalSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getTotalSalesByDateRange(startDate, endDate);
    }

    // Analytics methods
    public List<Object[]> getTransactionCountByMerchant() {
        return transactionRepository.findTransactionCountByMerchant();
    }

    public List<Object[]> getTransactionVolumeByMerchant() {
        return transactionRepository.findTransactionVolumeByMerchant();
    }

    public List<Object[]> getDailyTransactionStats(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.getDailyTransactionStats(startDate, endDate);
    }

    // Cleanup methods
    public int expirePendingTransactions(LocalDateTime expiryTime) {
        return transactionRepository.expirePendingTransactions(expiryTime);
    }

    private String generateTransactionReference() {
        return "TXN_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generatePaymentGatewayRef() {
        return "PG_" + System.currentTimeMillis();
    }

    private boolean processPaymentWithGateway(Transaction transaction, String paymentMethod) {
        // Integrate with actual payment gateway (Stripe, PayPal, etc.)
        // This is a mock implementation
        try {
            // Simulate payment processing
            Thread.sleep(1000);
            return Math.random() > 0.1; // 90% success rate for demo
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private boolean processRefundWithGateway(Transaction transaction, BigDecimal refundAmount) {
        // Integrate with payment gateway for refund
        // Mock implementation
        return true;
    }

    private void updateMerchantWallet(Merchant merchant, BigDecimal amount) {
        BigDecimal currentBalance = merchant.getWalletBalance();
        merchant.setWalletBalance(currentBalance.add(amount));
        merchantRepository.save(merchant);
    }

    private void deductFromMerchantWallet(Merchant merchant, BigDecimal amount) {
        BigDecimal currentBalance = merchant.getWalletBalance();
        if (currentBalance.compareTo(amount) >= 0) {
            merchant.setWalletBalance(currentBalance.subtract(amount));
            merchantRepository.save(merchant);
        } else {
            throw new RuntimeException("Insufficient merchant balance for refund");
        }
    }
}