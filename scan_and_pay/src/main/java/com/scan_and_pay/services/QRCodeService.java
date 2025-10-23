package com.scan_and_pay.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scan_and_pay.models.DynamicQRCode;
import com.scan_and_pay.models.Merchant;
import com.scan_and_pay.models.Transaction;
import com.scan_and_pay.repositories.DynamicQRCodeRepository;
import com.scan_and_pay.repositories.MerchantRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class QRCodeService {

    @Autowired
    private DynamicQRCodeRepository qrCodeRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Value("${app.qr.code.expiration-minutes:30}")
    private int qrCodeExpirationMinutes;

    @Value("${app.qr.code.base-url:https://scanpay.com}")
    private String appBaseUrl;

    // Get all QR codes
    public List<DynamicQRCode> getAllQRCodes() {
        return qrCodeRepository.findAll();
    }

    // Get all QR codes with pagination
    public Page<DynamicQRCode> getAllQRCodes(Pageable pageable) {
        return qrCodeRepository.findAll(pageable);
    }

    public DynamicQRCode generateDynamicQRCode(UUID merchantId, BigDecimal amount, String purpose, String description) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new RuntimeException("Merchant not found"));

        String qrData = generateQRCodeData(merchantId, amount, purpose);
        
        DynamicQRCode qrCode = new DynamicQRCode();
        qrCode.setMerchant(merchant);
        qrCode.setQrData(qrData);
        qrCode.setAmount(amount);
        qrCode.setPurpose(purpose);
        qrCode.setDescription(description);
        qrCode.setExpiresAt(LocalDateTime.now().plusMinutes(qrCodeExpirationMinutes));
        qrCode.setActive(true);

        return qrCodeRepository.save(qrCode);
    }

    public String generateQRCodeData(UUID merchantId, BigDecimal amount, String purpose) {
        // Generate QR data that can be scanned by the app
        String paymentUrl = String.format("%s/pay?merchant=%s&amount=%s&purpose=%s&timestamp=%d",
                appBaseUrl, merchantId, amount.toString(), purpose, System.currentTimeMillis());
        
        return paymentUrl;
    }

    public String generateQRCodeData(Transaction transaction) {
        // Generate QR data for a specific transaction
        return String.format("%s/pay?txn=%s", appBaseUrl, transaction.getTransactionRef());
    }

    public Optional<DynamicQRCode> getQRCodeById(UUID id) {
        return qrCodeRepository.findById(id);
    }

    public Optional<DynamicQRCode> getQRCodeByData(String qrData) {
        return qrCodeRepository.findByQrDataAndIsActiveTrue(qrData);
    }

    public DynamicQRCode markQRCodeAsScanned(UUID qrCodeId, UUID scannedBy) {
        DynamicQRCode qrCode = qrCodeRepository.findById(qrCodeId)
                .orElseThrow(() -> new RuntimeException("QR Code not found"));

        qrCode.setScannedAt(LocalDateTime.now());
        qrCode.setScannedBy(scannedBy);
        
        return qrCodeRepository.save(qrCode);
    }

    public void deactivateQRCode(UUID qrCodeId) {
        qrCodeRepository.findById(qrCodeId).ifPresent(qrCode -> {
            qrCode.setActive(false);
            qrCodeRepository.save(qrCode);
        });
    }

    public void cleanupExpiredQRCodes() {
        qrCodeRepository.deactivateExpiredQRCodes(LocalDateTime.now());
    }

    public boolean validateQRCode(String qrData) {
        Optional<DynamicQRCode> qrCodeOpt = qrCodeRepository.findByQrDataAndIsActiveTrue(qrData);
        if (qrCodeOpt.isPresent()) {
            DynamicQRCode qrCode = qrCodeOpt.get();
            return qrCode.isActive() && 
                   qrCode.getExpiresAt().isAfter(LocalDateTime.now()) && 
                   qrCode.getScannedAt() == null;
        }
        return false;
    }

    public List<DynamicQRCode> getActiveQRCodesByMerchant(UUID merchantId) {
        return qrCodeRepository.findByMerchantIdAndIsActiveTrue(merchantId);
    }

    public List<DynamicQRCode> getExpiredQRCodes() {
        return qrCodeRepository.findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime.now());
    }

    // Count methods for dashboard - ADD THESE METHODS
    public Long countActiveQRCodesByMerchant(UUID merchantId) {
        return qrCodeRepository.countActiveQRCodesByMerchant(merchantId);
    }

    public Long countAllQRCodes() {
        return qrCodeRepository.countAllQRCodes();
    }

    public Long countActiveQRCodes() {
        return qrCodeRepository.countActiveQRCodes();
    }

    public Long countByMerchantId(UUID merchantId) {
        return qrCodeRepository.countByMerchantId(merchantId);
    }
}