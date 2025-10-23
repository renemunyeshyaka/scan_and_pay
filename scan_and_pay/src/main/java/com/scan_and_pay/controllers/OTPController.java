package com.scan_and_pay.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.scan_and_pay.models.EmailOtp;
import com.scan_and_pay.models.PushOtp;
import com.scan_and_pay.services.OTPService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/otp")
public class OTPController {

    @Autowired
    private OTPService otpService;

    @PostMapping("/send-email")
    public ResponseEntity<?> sendEmailOTP(@RequestBody SendEmailOTPRequest request) {
        try {
            EmailOtp otp = otpService.sendEmailOTP(request.getEmail(), request.getType());
            
            var response = new HashMap<String, Object>();
            response.put("message", "OTP sent successfully");
            response.put("otpId", otp.getId());
            response.put("deliveryStatus", otp.getDeliveryStatus());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send OTP");
        }
    }

    @PostMapping("/send-push")
    public ResponseEntity<?> sendPushOTP(@RequestBody SendPushOTPRequest request) {
        try {
            PushOtp otp = otpService.sendPushOTP(
                request.getUserId(),
                request.getDeviceToken(),
                request.getDeviceType(),
                request.getType()
            );
            
            var response = new HashMap<String, Object>();
            response.put("message", "Push OTP sent successfully");
            response.put("otpId", otp.getId());
            response.put("deliveryStatus", otp.getDeliveryStatus());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send push OTP");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOTP(@RequestBody VerifyOTPRequest request) {
        try {
            boolean isValid = otpService.verifyOTP(
                request.getUserId(),
                request.getOtpCode(),
                request.getType()
            );
            
            var response = new HashMap<String, Object>();
            response.put("valid", isValid);
            response.put("userId", request.getUserId());
            response.put("type", request.getType());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Verification failed");
        }
    }

    @PostMapping("/resend/{otpId}")
    public ResponseEntity<?> resendOTP(@PathVariable UUID otpId) {
        try {
            otpService.resendOTP(otpId);
            return ResponseEntity.ok("OTP resent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to resend OTP");
        }
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Void> cleanupExpiredOTPs() {
        otpService.cleanupExpiredOTPs();
        return ResponseEntity.ok().build();
    }

    // Convenience endpoints
    @PostMapping("/send-login")
    public ResponseEntity<?> sendLoginOTP(@RequestParam String email) {
        try {
            EmailOtp otp = otpService.sendLoginOTP(email);
            return ResponseEntity.ok(Map.of(
                "message", "Login OTP sent successfully",
                "otpId", otp.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send login OTP");
        }
    }

    @PostMapping("/send-payment")
    public ResponseEntity<?> sendPaymentOTP(@RequestParam String email) {
        try {
            EmailOtp otp = otpService.sendPaymentOTP(email);
            return ResponseEntity.ok(Map.of(
                "message", "Payment OTP sent successfully",
                "otpId", otp.getId()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send payment OTP");
        }
    }

    // Request DTOs
    public static class SendEmailOTPRequest {
        private String email;
        private String type;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class SendPushOTPRequest {
        private UUID userId;
        private String deviceToken;
        private String deviceType;
        private String type;

        // Getters and setters
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public String getDeviceToken() { return deviceToken; }
        public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class VerifyOTPRequest {
        private UUID userId;
        private String otpCode;
        private String type;

        // Getters and setters
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public String getOtpCode() { return otpCode; }
        public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}