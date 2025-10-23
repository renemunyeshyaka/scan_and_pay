package com.scan_and_pay.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private Qr qr = new Qr();
    private Otp otp = new Otp();
    private Payment payment = new Payment();
    private Security security = new Security();
    private Push push = new Push();

    public static class Qr {
        private int expirationMinutes = 30;
        private String baseUrl = "https://scanpay.com/pay";
        private int size = 300;
        private String format = "PNG";

        // Getters and setters
        public int getExpirationMinutes() { return expirationMinutes; }
        public void setExpirationMinutes(int expirationMinutes) { this.expirationMinutes = expirationMinutes; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
    }

    public static class Otp {
        private int expirationMinutes = 10;
        private int length = 6;
        private int maxAttempts = 3;
        private int resendDelaySeconds = 60;

        // Getters and setters
        public int getExpirationMinutes() { return expirationMinutes; }
        public void setExpirationMinutes(int expirationMinutes) { this.expirationMinutes = expirationMinutes; }
        public int getLength() { return length; }
        public void setLength(int length) { this.length = length; }
        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
        public int getResendDelaySeconds() { return resendDelaySeconds; }
        public void setResendDelaySeconds(int resendDelaySeconds) { this.resendDelaySeconds = resendDelaySeconds; }
    }

    public static class Payment {
        private int timeoutMinutes = 15;
        private double maxAmount = 10000.00;
        private double minAmount = 0.01;
        private String defaultCurrency = "USD";
        private String[] allowedCurrencies = {"USD", "EUR", "GBP", "INR"};

        // Getters and setters
        public int getTimeoutMinutes() { return timeoutMinutes; }
        public void setTimeoutMinutes(int timeoutMinutes) { this.timeoutMinutes = timeoutMinutes; }
        public double getMaxAmount() { return maxAmount; }
        public void setMaxAmount(double maxAmount) { this.maxAmount = maxAmount; }
        public double getMinAmount() { return minAmount; }
        public void setMinAmount(double minAmount) { this.minAmount = minAmount; }
        public String getDefaultCurrency() { return defaultCurrency; }
        public void setDefaultCurrency(String defaultCurrency) { this.defaultCurrency = defaultCurrency; }
        public String[] getAllowedCurrencies() { return allowedCurrencies; }
        public void setAllowedCurrencies(String[] allowedCurrencies) { this.allowedCurrencies = allowedCurrencies; }
    }

    public static class Security {
        private Jwt jwt = new Jwt();

        public static class Jwt {
            private String secret = "myVerySecretKeyForJWTGenerationInScanPaySystem2024";
            private long expiration = 86400000; // 24 hours
            private long refreshExpiration = 604800000; // 7 days

            // Getters and setters
            public String getSecret() { return secret; }
            public void setSecret(String secret) { this.secret = secret; }
            public long getExpiration() { return expiration; }
            public void setExpiration(long expiration) { this.expiration = expiration; }
            public long getRefreshExpiration() { return refreshExpiration; }
            public void setRefreshExpiration(long refreshExpiration) { this.refreshExpiration = refreshExpiration; }
        }

        // Getters and setters
        public Jwt getJwt() { return jwt; }
        public void setJwt(Jwt jwt) { this.jwt = jwt; }
    }

    public static class Push {
        private boolean enabled = true;
        private int timeout = 5000;
        private Fcm fcm = new Fcm();
        private Apple apple = new Apple();

        public static class Fcm {
            private String serverKey = "";
            private String apiUrl = "https://fcm.googleapis.com/fcm/send";

            // Getters and setters
            public String getServerKey() { return serverKey; }
            public void setServerKey(String serverKey) { this.serverKey = serverKey; }
            public String getApiUrl() { return apiUrl; }
            public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
        }

        public static class Apple {
            private String pushUrl = "https://api.sandbox.push.apple.com";
            private String teamId = "";
            private String keyId = "";
            private String bundleId = "com.scan_and_pay.app";
            private String keyFilePath = "";

            // Getters and setters
            public String getPushUrl() { return pushUrl; }
            public void setPushUrl(String pushUrl) { this.pushUrl = pushUrl; }
            public String getTeamId() { return teamId; }
            public void setTeamId(String teamId) { this.teamId = teamId; }
            public String getKeyId() { return keyId; }
            public void setKeyId(String keyId) { this.keyId = keyId; }
            public String getBundleId() { return bundleId; }
            public void setBundleId(String bundleId) { this.bundleId = bundleId; }
            public String getKeyFilePath() { return keyFilePath; }
            public void setKeyFilePath(String keyFilePath) { this.keyFilePath = keyFilePath; }
        }

        // Getters and setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        public Fcm getFcm() { return fcm; }
        public void setFcm(Fcm fcm) { this.fcm = fcm; }
        public Apple getApple() { return apple; }
        public void setApple(Apple apple) { this.apple = apple; }
    }

    // Getters and setters
    public Qr getQr() { return qr; }
    public void setQr(Qr qr) { this.qr = qr; }
    public Otp getOtp() { return otp; }
    public void setOtp(Otp otp) { this.otp = otp; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }
    public Push getPush() { return push; }
    public void setPush(Push push) { this.push = push; }
}