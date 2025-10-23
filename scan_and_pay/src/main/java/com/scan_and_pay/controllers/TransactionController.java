package com.scan_and_pay.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.scan_and_pay.models.Transaction;
import com.scan_and_pay.services.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody CreateTransactionRequest request) {
        try {
            Transaction transaction = transactionService.createTransaction(
                request.getMerchantId(), 
                request.getAmount(), 
                request.getDescription()
            );
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable UUID id) {
        return transactionService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reference/{ref}")
    public ResponseEntity<Transaction> getTransactionByReference(@PathVariable String ref) {
        return transactionService.getTransactionByReference(ref)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<Transaction>> getTransactionsByMerchant(@PathVariable UUID merchantId) {
        List<Transaction> transactions = transactionService.getTransactionsByMerchant(merchantId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Transaction>> getTransactionsByCustomer(@PathVariable UUID customerId) {
        List<Transaction> transactions = transactionService.getTransactionsByCustomer(customerId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Transaction>> getTransactionsByStatus(@PathVariable String status) {
        List<Transaction> transactions = transactionService.getTransactionsByStatus(status);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/process-payment")
    public ResponseEntity<Transaction> processPayment(@RequestBody ProcessPaymentRequest request) {
        try {
            Transaction transaction = transactionService.processPayment(
                request.getTransactionRef(),
                request.getCustomerId(),
                request.getPaymentMethod()
            );
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<Transaction> refundTransaction(
            @PathVariable UUID id,
            @RequestParam(required = false) BigDecimal amount) {
        try {
            BigDecimal refundAmount = amount != null ? amount : transactionService.getTransactionById(id)
                    .orElseThrow(() -> new RuntimeException("Transaction not found"))
                    .getAmount();
            
            Transaction refundedTransaction = transactionService.refundTransaction(id, refundAmount);
            return ResponseEntity.ok(refundedTransaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/stats/merchant/{merchantId}")
    public ResponseEntity<?> getMerchantStats(
            @PathVariable UUID merchantId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        try {
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }

            BigDecimal totalSales = transactionService.getTotalSalesByMerchant(merchantId, startDate, endDate);
            
            var stats = new HashMap<String, Object>();
            stats.put("merchantId", merchantId);
            stats.put("totalSales", totalSales);
            stats.put("period", Map.of("startDate", startDate, "endDate", endDate));
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating stats");
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<Transaction>> getTransactionsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        
        List<Transaction> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    // Request DTOs
    public static class CreateTransactionRequest {
        private UUID merchantId;
        private BigDecimal amount;
        private String description;

        // Getters and setters
        public UUID getMerchantId() { return merchantId; }
        public void setMerchantId(UUID merchantId) { this.merchantId = merchantId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class ProcessPaymentRequest {
        private String transactionRef;
        private UUID customerId;
        private String paymentMethod;

        // Getters and setters
        public String getTransactionRef() { return transactionRef; }
        public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
        public UUID getCustomerId() { return customerId; }
        public void setCustomerId(UUID customerId) { this.customerId = customerId; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }
}