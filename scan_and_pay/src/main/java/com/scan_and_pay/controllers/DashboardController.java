package com.scan_and_pay.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.scan_and_pay.services.QRCodeService;
import com.scan_and_pay.services.TransactionService;
import com.scan_and_pay.services.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserService userService;

    @Autowired
    private QRCodeService qrCodeService;

    @GetMapping("/merchant/{merchantId}")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('ADMIN')")
    public ResponseEntity<?> getMerchantDashboard(@PathVariable UUID merchantId) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(30);
            LocalDateTime endDate = LocalDateTime.now();

            // Get transaction statistics
            BigDecimal totalSales = transactionService.getTotalSalesByMerchant(merchantId, startDate, endDate);
            Long transactionCount = transactionService.countSuccessfulTransactionsByMerchant(merchantId);

            // Get QR code statistics
            Long activeQRCodes = qrCodeService.countActiveQRCodesByMerchant(merchantId);

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("merchantId", merchantId);
            dashboard.put("totalSales", totalSales);
            dashboard.put("transactionCount", transactionCount);
            dashboard.put("activeQRCodes", activeQRCodes);
            
            Map<String, Object> period = new HashMap<>();
            period.put("startDate", startDate);
            period.put("endDate", endDate);
            dashboard.put("period", period);

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading dashboard");
        }
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminDashboard() {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(30);
            LocalDateTime endDate = LocalDateTime.now();

            // Get overall statistics
            Long totalUsers = userService.countAllUsers();
            Long totalMerchants = userService.countAllMerchants();
            Long totalAdmins = userService.countAllAdmins();
            BigDecimal totalSales = transactionService.getTotalSalesByDateRange(startDate, endDate);
            Long totalTransactions = transactionService.countSuccessfulTransactionsByDateRange(startDate, endDate);
            Long totalQRCodes = qrCodeService.countAllQRCodes();
            Long verifiedMerchants = userService.countVerifiedMerchants();

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalUsers", totalUsers);
            dashboard.put("totalMerchants", totalMerchants);
            dashboard.put("totalAdmins", totalAdmins);
            dashboard.put("totalSales", totalSales);
            dashboard.put("totalTransactions", totalTransactions);
            dashboard.put("totalQRCodes", totalQRCodes);
            dashboard.put("verifiedMerchants", verifiedMerchants);
            
            Map<String, Object> period = new HashMap<>();
            period.put("startDate", startDate);
            period.put("endDate", endDate);
            dashboard.put("period", period);

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading admin dashboard");
        }
    }

    @GetMapping("/stats/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTransactionStats(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        try {
            if (startDate == null) startDate = LocalDateTime.now().minusDays(30);
            if (endDate == null) endDate = LocalDateTime.now();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalSales", transactionService.getTotalSalesByDateRange(startDate, endDate));
            stats.put("totalTransactions", transactionService.countSuccessfulTransactionsByDateRange(startDate, endDate));
            stats.put("transactionCountByMerchant", transactionService.getTransactionCountByMerchant());
            stats.put("transactionVolumeByMerchant", transactionService.getTransactionVolumeByMerchant());
            stats.put("dailyStats", transactionService.getDailyTransactionStats(startDate, endDate));
            
            Map<String, Object> period = new HashMap<>();
            period.put("startDate", startDate);
            period.put("endDate", endDate);
            stats.put("period", period);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading transaction stats");
        }
    }
}