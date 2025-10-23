package com.scan_and_pay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class EncryptionConfig {

    @Value("${app.encryption.secret-key:ThisIsASecretKeyForEncryption123}")
    private String secretKey;

    @Value("${app.encryption.algorithm:AES}")
    private String algorithm;

    @Bean
    SecretKey encryptionKey() {
        // Ensure the key is exactly 16, 24, or 32 bytes for AES
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] validKeyBytes = new byte[32]; // 256-bit key
        
        // Copy and pad with zeros if necessary
        System.arraycopy(keyBytes, 0, validKeyBytes, 0, Math.min(keyBytes.length, validKeyBytes.length));
        
        return new SecretKeySpec(validKeyBytes, algorithm);
    }

    public String getAlgorithm() {
        return algorithm;
    }
}