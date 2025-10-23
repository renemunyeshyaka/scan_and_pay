package com.scan_and_pay.config;

import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QRCodeConfig {

    @Value("${app.qr.code.expiration-minutes:30}")
    private int qrCodeExpirationMinutes;

    @Value("${app.qr.code.base-url:https://scanpay.com/pay}")
    private String baseUrl;

    @Value("${app.qr.code.size:300}")
    private int qrCodeSize;

    @Bean
    public QRCodeWriter qrCodeWriter() {
        return new QRCodeWriter();
    }

    public int getQrCodeExpirationMinutes() {
        return qrCodeExpirationMinutes;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getQrCodeSize() {
        return qrCodeSize;
    }
}