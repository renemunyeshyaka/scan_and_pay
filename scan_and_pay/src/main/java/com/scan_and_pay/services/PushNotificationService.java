package com.scan_and_pay.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PushNotificationService {

    @Value("${fcm.server.key:}")
    private String fcmServerKey;

    @Value("${fcm.api.url:https://fcm.googleapis.com/fcm/send}")
    private String fcmApiUrl;

    @Value("${apple.push.url:}")
    private String applePushUrl;

    @Value("${apple.push.team.id:}")
    private String appleTeamId;

    @Value("${apple.push.key.id:}")
    private String appleKeyId;

    @Value("${apple.push.bundle.id:}")
    private String appleBundleId;

    private final RestTemplate restTemplate;

    public PushNotificationService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Send OTP push notification to a device
     */
    public boolean sendOTPPush(String deviceToken, String title, String body) {
        // Determine device type based on token format or use a default
        String deviceType = determineDeviceType(deviceToken);
        
        switch (deviceType.toUpperCase()) {
            case "ANDROID":
                return sendFCMNotification(deviceToken, title, body);
            case "IOS":
                return sendAPNSNotification(deviceToken, title, body);
            default:
                // Try FCM first, then APNS as fallback
                return sendFCMNotification(deviceToken, title, body) || 
                       sendAPNSNotification(deviceToken, title, body);
        }
    }

    /**
     * Send payment success push notification
     */
    public boolean sendPaymentSuccessPush(String deviceToken, String merchantName, String amount, String transactionRef) {
        String title = "Payment Successful";
        String body = String.format("You have successfully paid %s to %s. Ref: %s", 
                                   amount, merchantName, transactionRef);
        
        return sendOTPPush(deviceToken, title, body);
    }

    /**
     * Send payment failure push notification
     */
    public boolean sendPaymentFailedPush(String deviceToken, String merchantName, String amount, String reason) {
        String title = "Payment Failed";
        String body = String.format("Payment of %s to %s failed. Reason: %s", 
                                   amount, merchantName, reason);
        
        return sendOTPPush(deviceToken, title, body);
    }

    /**
     * Send refund notification
     */
    public boolean sendRefundPush(String deviceToken, String amount, String transactionRef) {
        String title = "Refund Processed";
        String body = String.format("A refund of %s has been processed for transaction %s", 
                                   amount, transactionRef);
        
        return sendOTPPush(deviceToken, title, body);
    }

    /**
     * Send login notification
     */
    public boolean sendLoginNotification(String deviceToken, String deviceInfo, String location) {
        String title = "New Login Detected";
        String body = String.format("New login from %s at %s. If this wasn't you, please secure your account.", 
                                   deviceInfo, location);
        
        return sendOTPPush(deviceToken, title, body);
    }

    /**
     * Send FCM (Firebase Cloud Messaging) notification for Android devices
     */
    private boolean sendFCMNotification(String deviceToken, String title, String body) {
        try {
            if (fcmServerKey == null || fcmServerKey.isEmpty()) {
                System.err.println("FCM Server Key not configured");
                return false;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "key=" + fcmServerKey);

            Map<String, Object> notification = new HashMap<>();
            notification.put("title", title);
            notification.put("body", body);
            notification.put("sound", "default");

            Map<String, Object> data = new HashMap<>();
            data.put("type", "OTP");
            data.put("timestamp", System.currentTimeMillis());

            Map<String, Object> message = new HashMap<>();
            message.put("to", deviceToken);
            message.put("notification", notification);
            message.put("data", data);
            message.put("priority", "high");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(message, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(fcmApiUrl, request, String.class);

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            System.err.println("FCM notification failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send APNS (Apple Push Notification Service) notification for iOS devices
     */
    private boolean sendAPNSNotification(String deviceToken, String title, String body) {
        try {
            if (applePushUrl == null || applePushUrl.isEmpty()) {
                System.err.println("APNS URL not configured");
                return false;
            }

            // For APNS, you would typically use a JWT token for authentication
            // This is a simplified version - in production, you'd need proper JWT generation
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("authorization", "bearer " + generateAPNSToken());
            headers.set("apns-topic", appleBundleId);
            headers.set("apns-priority", "10");
            headers.set("apns-push-type", "alert");

            Map<String, Object> aps = new HashMap<>();
            aps.put("alert", Map.of("title", title, "body", body));
            aps.put("sound", "default");
            aps.put("badge", 1);

            Map<String, Object> payload = new HashMap<>();
            payload.put("aps", aps);
            payload.put("type", "OTP");
            payload.put("timestamp", System.currentTimeMillis());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            String url = applePushUrl + "/3/device/" + deviceToken;
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            System.err.println("APNS notification failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generate APNS JWT token (simplified - implement proper JWT generation in production)
     */
    private String generateAPNSToken() {
        // In production, you would:
        // 1. Load your .p8 key file
        // 2. Generate a JWT token with:
        //    - iss: Your Team ID
        //    - iat: Current time
        //    - exp: Current time + 1 hour
        // 3. Sign with your private key
        
        // This is a placeholder implementation
        System.out.println("APNS token generation would happen here");
        return "dummy-apns-token";
    }

    /**
     * Determine device type based on token format
     */
    private String determineDeviceType(String deviceToken) {
        if (deviceToken == null || deviceToken.isEmpty()) {
            return "UNKNOWN";
        }
        
        // FCM tokens are typically longer than APNS tokens
        if (deviceToken.length() > 100) {
            return "ANDROID";
        } else if (deviceToken.length() >= 64 && deviceToken.length() <= 100) {
            return "IOS";
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * Send bulk notifications to multiple devices
     */
    public boolean sendBulkNotification(java.util.List<String> deviceTokens, String title, String body) {
        boolean allSent = true;
        
        for (String token : deviceTokens) {
            if (!sendOTPPush(token, title, body)) {
                allSent = false;
            }
        }
        
        return allSent;
    }

    /**
     * Validate device token format
     */
    public boolean isValidDeviceToken(String deviceToken) {
        if (deviceToken == null || deviceToken.trim().isEmpty()) {
            return false;
        }
        
        // Basic validation - in production, you might want more sophisticated checks
        return deviceToken.length() >= 32 && deviceToken.length() <= 256;
    }

    /**
     * Test notification service connectivity
     */
    public boolean testConnection() {
        // Test with a dummy token to check configuration
        String testToken = "test-token-123";
        return sendOTPPush(testToken, "Test Notification", "This is a test message");
    }
}