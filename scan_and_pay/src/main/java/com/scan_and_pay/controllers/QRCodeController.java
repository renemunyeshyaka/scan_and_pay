package com.scan_and_pay.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.scan_and_pay.models.DynamicQRCode;
import com.scan_and_pay.services.QRCodeService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/qrcodes")
public class QRCodeController {

    @Autowired
    private QRCodeService qrCodeService;

    @PostMapping
    public ResponseEntity<DynamicQRCode> generateQRCode(@RequestBody GenerateQRCodeRequest request) {
        try {
            DynamicQRCode qrCode = qrCodeService.generateDynamicQRCode(
                request.getMerchantId(),
                request.getAmount(),
                request.getPurpose(),
                request.getDescription()
            );
            return ResponseEntity.ok(qrCode);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<DynamicQRCode>> getAllQRCodes() {
        List<DynamicQRCode> qrCodes = qrCodeService.getAllQRCodes();
        return ResponseEntity.ok(qrCodes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DynamicQRCode> getQRCodeById(@PathVariable UUID id) {
        return qrCodeService.getQRCodeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/data/{qrData}")
    public ResponseEntity<DynamicQRCode> getQRCodeByData(@PathVariable String qrData) {
        return qrCodeService.getQRCodeByData(qrData)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<DynamicQRCode>> getQRCodesByMerchant(@PathVariable UUID merchantId) {
        List<DynamicQRCode> qrCodes = qrCodeService.getActiveQRCodesByMerchant(merchantId);
        return ResponseEntity.ok(qrCodes);
    }

    @PostMapping("/{id}/scan")
    public ResponseEntity<DynamicQRCode> markAsScanned(
            @PathVariable UUID id,
            @RequestParam UUID scannedBy) {
        try {
            DynamicQRCode qrCode = qrCodeService.markQRCodeAsScanned(id, scannedBy);
            return ResponseEntity.ok(qrCode);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateQRCode(@PathVariable UUID id) {
        qrCodeService.deactivateQRCode(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateQRCode(@RequestParam String qrData) {
        boolean isValid = qrCodeService.validateQRCode(qrData);
        
        var response = new HashMap<String, Object>();
        response.put("valid", isValid);
        response.put("qrData", qrData);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<DynamicQRCode>> getExpiredQRCodes() {
        List<DynamicQRCode> expiredQRCodes = qrCodeService.getExpiredQRCodes();
        return ResponseEntity.ok(expiredQRCodes);
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Void> cleanupExpiredQRCodes() {
        qrCodeService.cleanupExpiredQRCodes();
        return ResponseEntity.ok().build();
    }

    // Request DTO
    public static class GenerateQRCodeRequest {
        private UUID merchantId;
        private BigDecimal amount;
        private String purpose;
        private String description;

        // Getters and setters
        public UUID getMerchantId() { return merchantId; }
        public void setMerchantId(UUID merchantId) { this.merchantId = merchantId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}