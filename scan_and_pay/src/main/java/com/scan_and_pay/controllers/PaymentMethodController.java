package com.scan_and_pay.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.scan_and_pay.models.PaymentMethod;
import com.scan_and_pay.services.PaymentMethodService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @PostMapping
    public ResponseEntity<PaymentMethod> addPaymentMethod(@RequestBody AddPaymentMethodRequest request) {
        try {
            PaymentMethod paymentMethod = paymentMethodService.addPaymentMethod(
                request.getUserId(),
                request.getMethodType(),
                request.getProviderName(),
                request.getAccountLastFour(),
                request.getToken(),
                request.getMetadata()
            );
            return ResponseEntity.ok(paymentMethod);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentMethod>> getPaymentMethodsByUser(@PathVariable UUID userId) {
        List<PaymentMethod> paymentMethods = paymentMethodService.getPaymentMethodsByUser(userId);
        return ResponseEntity.ok(paymentMethods);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethod> getPaymentMethodById(@PathVariable UUID id) {
        return paymentMethodService.getPaymentMethodById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethod> updatePaymentMethod(
            @PathVariable UUID id,
            @RequestBody PaymentMethod paymentMethodDetails) {
        try {
            PaymentMethod updatedMethod = paymentMethodService.updatePaymentMethod(id, paymentMethodDetails);
            return ResponseEntity.ok(updatedMethod);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentMethod(@PathVariable UUID id) {
        paymentMethodService.deletePaymentMethod(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/set-default")
    public ResponseEntity<PaymentMethod> setDefaultPaymentMethod(
            @PathVariable UUID id,
            @RequestParam UUID userId) {
        try {
            PaymentMethod defaultMethod = paymentMethodService.setDefaultPaymentMethod(userId, id);
            return ResponseEntity.ok(defaultMethod);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/user/{userId}/default")
    public ResponseEntity<PaymentMethod> getDefaultPaymentMethod(@PathVariable UUID userId) {
        return paymentMethodService.getDefaultPaymentMethod(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<Void> verifyPaymentMethod(@PathVariable UUID id) {
        paymentMethodService.verifyPaymentMethod(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Void> cleanupExpiredPaymentMethods() {
        paymentMethodService.expirePaymentMethods();
        return ResponseEntity.ok().build();
    }

    // Request DTO
    public static class AddPaymentMethodRequest {
        private UUID userId;
        private String methodType;
        private String providerName;
        private String accountLastFour;
        private String token;
        private String metadata;

        // Getters and setters
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public String getMethodType() { return methodType; }
        public void setMethodType(String methodType) { this.methodType = methodType; }
        public String getProviderName() { return providerName; }
        public void setProviderName(String providerName) { this.providerName = providerName; }
        public String getAccountLastFour() { return accountLastFour; }
        public void setAccountLastFour(String accountLastFour) { this.accountLastFour = accountLastFour; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }
    }
}