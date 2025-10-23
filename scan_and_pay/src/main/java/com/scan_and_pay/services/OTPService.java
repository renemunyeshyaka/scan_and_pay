package com.scan_and_pay.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scan_and_pay.models.EmailOtp;
import com.scan_and_pay.models.Otp;
import com.scan_and_pay.models.PushOtp;
import com.scan_and_pay.models.User;
import com.scan_and_pay.repositories.EmailOtpRepository;
import com.scan_and_pay.repositories.OtpRepository;
import com.scan_and_pay.repositories.PushOtpRepository;
import com.scan_and_pay.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class OTPService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailOtpRepository emailOtpRepository;

    @Autowired
    private PushOtpRepository pushOtpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PushNotificationService pushNotificationService;

    @Value("${otp.expiration.minutes:10}")
    private int otpExpirationMinutes;

    @Value("${otp.length:6}")
    private int otpLength;

    @Value("${otp.max.attempts:3}")
    private int maxAttempts;

    public EmailOtp sendEmailOTP(String email, String type) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String otpCode = generateOTP();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        // Deactivate any existing OTPs for this user and type
        deactivateExistingOTPs(user.getId(), type);

        EmailOtp emailOtp = new EmailOtp(otpCode, email, type, user.getId());
        emailOtp.setExpiresAt(expiresAt);
        emailOtp.setMaxAttempts(maxAttempts);

        // Set email content based on type
        setEmailContent(emailOtp, type, otpCode);

        EmailOtp savedOtp = emailOtpRepository.save(emailOtp);

        // Send email
        boolean emailSent = emailService.sendOTPEmail(email, emailOtp.getEmailSubject(), emailOtp.getEmailBody());
        if (emailSent) {
            savedOtp.setDelivered(true);
            savedOtp.setDeliveredAt(LocalDateTime.now());
            savedOtp.setDeliveryStatus("SENT");
        } else {
            savedOtp.setDeliveryStatus("FAILED");
        }

        return emailOtpRepository.save(savedOtp);
    }

    public PushOtp sendPushOTP(UUID userId, String deviceToken, String deviceType, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otpCode = generateOTP();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        // Deactivate any existing OTPs for this user and type
        deactivateExistingOTPs(user.getId(), type);

        PushOtp pushOtp = new PushOtp(otpCode, user.getEmail(), type, user.getId(), deviceToken);
        pushOtp.setExpiresAt(expiresAt);
        pushOtp.setMaxAttempts(maxAttempts);
        pushOtp.setDeviceType(deviceType);

        // Set push notification content
        setPushContent(pushOtp, type, otpCode);

        PushOtp savedOtp = pushOtpRepository.save(pushOtp);

        // Send push notification
        boolean pushSent = pushNotificationService.sendOTPPush(deviceToken, pushOtp.getPushTitle(), pushOtp.getPushBody());
        if (pushSent) {
            savedOtp.setDelivered(true);
            savedOtp.setDeliveredAt(LocalDateTime.now());
            savedOtp.setDeliveryStatus("SENT");
        } else {
            savedOtp.setDeliveryStatus("FAILED");
        }

        return pushOtpRepository.save(savedOtp);
    }

    public EmailOtp sendEmailVerificationOTP(User user) {
        return sendEmailOTP(user.getEmail(), "EMAIL_VERIFICATION");
    }

    public EmailOtp sendLoginOTP(String email) {
        return sendEmailOTP(email, "LOGIN");
    }

    public EmailOtp sendPasswordResetOTP(String email) {
        return sendEmailOTP(email, "PASSWORD_RESET");
    }

    public EmailOtp sendPaymentOTP(String email) {
        return sendEmailOTP(email, "PAYMENT");
    }

    public boolean verifyOTP(UUID userId, String otpCode, String type) {
        Optional<Otp> otpOpt = otpRepository.findTopByUserIdAndTypeAndIsUsedFalseOrderByCreatedAtDesc(userId, type);
        
        if (otpOpt.isPresent()) {
            Otp otp = otpOpt.get();
            
            if (!otp.isValid()) {
                return false;
            }

            if (otp.getCode().equals(otpCode)) {
                otp.setUsed(true);
                otp.setUsedAt(LocalDateTime.now());
                otpRepository.save(otp);
                return true;
            } else {
                otp.incrementAttempts();
                otpRepository.save(otp);
                return false;
            }
        }
        return false;
    }

    public boolean verifyEmailOTP(UUID userId, String otpCode) {
        return verifyOTP(userId, otpCode, "EMAIL_VERIFICATION");
    }

    public boolean verifyLoginOTP(UUID userId, String otpCode) {
        return verifyOTP(userId, otpCode, "LOGIN");
    }

    public boolean verifyPaymentOTP(UUID userId, String otpCode) {
        return verifyOTP(userId, otpCode, "PAYMENT");
    }

    public void resendOTP(UUID otpId) {
        otpRepository.findById(otpId).ifPresent(otp -> {
            if (otp instanceof EmailOtp) {
                EmailOtp emailOtp = (EmailOtp) otp;
                boolean emailSent = emailService.sendOTPEmail(
                    emailOtp.getDestination(), 
                    emailOtp.getEmailSubject(), 
                    emailOtp.getEmailBody()
                );
                if (emailSent) {
                    emailOtp.setDelivered(true);
                    emailOtp.setDeliveredAt(LocalDateTime.now());
                    emailOtp.setDeliveryStatus("SENT");
                    emailOtpRepository.save(emailOtp);
                }
            } else if (otp instanceof PushOtp) {
                PushOtp pushOtp = (PushOtp) otp;
                boolean pushSent = pushNotificationService.sendOTPPush(
                    pushOtp.getDeviceToken(), 
                    pushOtp.getPushTitle(), 
                    pushOtp.getPushBody()
                );
                if (pushSent) {
                    pushOtp.setDelivered(true);
                    pushOtp.setDeliveredAt(LocalDateTime.now());
                    pushOtp.setDeliveryStatus("SENT");
                    pushOtpRepository.save(pushOtp);
                }
            }
        });
    }

    public void cleanupExpiredOTPs() {
        otpRepository.deleteByExpiresAtBeforeAndIsUsedFalse(LocalDateTime.now());
    }

    private String generateOTP() {
        int min = (int) Math.pow(10, otpLength - 1);
        int max = (int) Math.pow(10, otpLength) - 1;
        int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
        return String.valueOf(randomNum);
    }

    private void deactivateExistingOTPs(UUID userId, String type) {
        List<Otp> existingOtps = otpRepository.findByUserIdAndTypeAndIsUsedFalse(userId, type);
        existingOtps.forEach(otp -> {
            otp.setUsed(true);
            otpRepository.save(otp);
        });
    }

    private void setEmailContent(EmailOtp emailOtp, String type, String otpCode) {
        switch (type) {
            case "EMAIL_VERIFICATION":
                emailOtp.setEmailSubject("Verify Your Email Address");
                emailOtp.setEmailBody(String.format(
                    "Your email verification code is: %s. This code will expire in %d minutes.",
                    otpCode, otpExpirationMinutes
                ));
                break;
            case "LOGIN":
                emailOtp.setEmailSubject("Your Login Verification Code");
                emailOtp.setEmailBody(String.format(
                    "Your login verification code is: %s. This code will expire in %d minutes.",
                    otpCode, otpExpirationMinutes
                ));
                break;
            case "PASSWORD_RESET":
                emailOtp.setEmailSubject("Password Reset Verification Code");
                emailOtp.setEmailBody(String.format(
                    "Your password reset code is: %s. This code will expire in %d minutes.",
                    otpCode, otpExpirationMinutes
                ));
                break;
            case "PAYMENT":
                emailOtp.setEmailSubject("Payment Authorization Code");
                emailOtp.setEmailBody(String.format(
                    "Your payment authorization code is: %s. This code will expire in %d minutes.",
                    otpCode, otpExpirationMinutes
                ));
                break;
            default:
                emailOtp.setEmailSubject("Your Verification Code");
                emailOtp.setEmailBody(String.format(
                    "Your verification code is: %s. This code will expire in %d minutes.",
                    otpCode, otpExpirationMinutes
                ));
        }
    }

    private void setPushContent(PushOtp pushOtp, String type, String otpCode) {
        switch (type) {
            case "LOGIN":
                pushOtp.setPushTitle("Login Verification");
                pushOtp.setPushBody(String.format("Your login code: %s", otpCode));
                break;
            case "PAYMENT":
                pushOtp.setPushTitle("Payment Authorization");
                pushOtp.setPushBody(String.format("Your payment code: %s", otpCode));
                break;
            default:
                pushOtp.setPushTitle("Verification Code");
                pushOtp.setPushBody(String.format("Your code: %s", otpCode));
        }
    }
}