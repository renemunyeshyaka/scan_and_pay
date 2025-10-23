package com.scan_and_pay.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scan_and_pay.models.PaymentMethod;
import com.scan_and_pay.models.User;
import com.scan_and_pay.repositories.PaymentMethodRepository;
import com.scan_and_pay.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncryptionService encryptionService;

    public PaymentMethod addPaymentMethod(UUID userId, String methodType, String providerName, 
                                        String accountLastFour, String rawToken, String metadata) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Encrypt the token before storing
        String encryptedToken = encryptionService.encrypt(rawToken);

        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setUser(user);
        paymentMethod.setMethodType(methodType);
        paymentMethod.setProviderName(providerName);
        paymentMethod.setAccountLastFour(accountLastFour);
        paymentMethod.setToken(encryptedToken);
        paymentMethod.setMetadata(metadata);
        paymentMethod.setActive(true);
        paymentMethod.setVerified(false);

        // If this is the first payment method, set it as default
        List<PaymentMethod> existingMethods = paymentMethodRepository.findByUserIdAndIsActiveTrue(userId);
        if (existingMethods.isEmpty()) {
            paymentMethod.setDefault(true);
        }

        return paymentMethodRepository.save(paymentMethod);
    }

    public Optional<PaymentMethod> getPaymentMethodById(UUID id) {
        return paymentMethodRepository.findById(id);
    }

    public List<PaymentMethod> getPaymentMethodsByUser(UUID userId) {
        return paymentMethodRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public PaymentMethod updatePaymentMethod(UUID id, PaymentMethod paymentMethodDetails) {
        return paymentMethodRepository.findById(id).map(paymentMethod -> {
            if (paymentMethodDetails.getProviderName() != null) 
                paymentMethod.setProviderName(paymentMethodDetails.getProviderName());
            if (paymentMethodDetails.getAccountLastFour() != null) 
                paymentMethod.setAccountLastFour(paymentMethodDetails.getAccountLastFour());
            if (paymentMethodDetails.getMetadata() != null) 
                paymentMethod.setMetadata(paymentMethodDetails.getMetadata());
            
            return paymentMethodRepository.save(paymentMethod);
        }).orElseThrow(() -> new RuntimeException("Payment method not found with id: " + id));
    }

    public void deletePaymentMethod(UUID id) {
        paymentMethodRepository.findById(id).ifPresent(paymentMethod -> {
            paymentMethod.setActive(false);
            paymentMethodRepository.save(paymentMethod);
        });
    }

    public PaymentMethod setDefaultPaymentMethod(UUID userId, UUID paymentMethodId) {
        // First, unset all other default payment methods for this user
        List<PaymentMethod> userMethods = paymentMethodRepository.findByUserIdAndIsActiveTrue(userId);
        userMethods.forEach(method -> {
            if (method.isDefault()) {
                method.setDefault(false);
                paymentMethodRepository.save(method);
            }
        });

        // Set the new default
        PaymentMethod newDefault = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));
        
        if (!newDefault.getUser().getId().equals(userId)) {
            throw new RuntimeException("Payment method does not belong to user");
        }

        newDefault.setDefault(true);
        return paymentMethodRepository.save(newDefault);
    }

    public Optional<PaymentMethod> getDefaultPaymentMethod(UUID userId) {
        return paymentMethodRepository.findByUserIdAndIsDefaultTrueAndIsActiveTrue(userId);
    }

    public void verifyPaymentMethod(UUID paymentMethodId) {
        paymentMethodRepository.findById(paymentMethodId).ifPresent(paymentMethod -> {
            paymentMethod.setVerified(true);
            paymentMethodRepository.save(paymentMethod);
        });
    }

    public String getDecryptedToken(UUID paymentMethodId) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));
        
        return encryptionService.decrypt(paymentMethod.getToken());
    }

    public void expirePaymentMethods() {
        List<PaymentMethod> expiredMethods = paymentMethodRepository.findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime.now());
        expiredMethods.forEach(method -> {
            method.setActive(false);
            paymentMethodRepository.save(method);
        });
    }
}