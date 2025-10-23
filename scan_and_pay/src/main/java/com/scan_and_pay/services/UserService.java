package com.scan_and_pay.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scan_and_pay.models.Admin;
import com.scan_and_pay.models.Merchant;
import com.scan_and_pay.models.User;
import com.scan_and_pay.repositories.AdminRepository;
import com.scan_and_pay.repositories.MerchantRepository;
import com.scan_and_pay.repositories.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OTPService otpService;

    // Spring Security UserDetailsService implementation
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole())
                .build();
    }

    // User methods
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        
        // Send email verification OTP
        otpService.sendEmailVerificationOTP(savedUser);
        
        return savedUser;
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(UUID id, User userDetails) {
        return userRepository.findById(id).map(user -> {
            if (userDetails.getName() != null) user.setName(userDetails.getName());
            if (userDetails.getPhone() != null) user.setPhone(userDetails.getPhone());
            if (userDetails.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    public boolean verifyEmail(UUID userId, String otpCode) {
        boolean verified = otpService.verifyEmailOTP(userId, otpCode);
        if (verified) {
            userRepository.findById(userId).ifPresent(user -> {
                user.setEmailVerified(true);
                userRepository.save(user);
            });
        }
        return verified;
    }
    
    

    // Merchant methods
    public Merchant createMerchant(Merchant merchant) {
        merchant.setPassword(passwordEncoder.encode(merchant.getPassword()));
        return merchantRepository.save(merchant);
    }

    public Optional<Merchant> getMerchantById(UUID id) {
        return merchantRepository.findById(id);
    }

    public List<Merchant> getAllMerchants() {
        return merchantRepository.findAll();
    }

    public Merchant updateMerchant(UUID id, Merchant merchantDetails) {
        return merchantRepository.findById(id).map(merchant -> {
            if (merchantDetails.getBusinessName() != null) 
                merchant.setBusinessName(merchantDetails.getBusinessName());
            if (merchantDetails.getBusinessRegistrationNumber() != null) 
                merchant.setBusinessRegistrationNumber(merchantDetails.getBusinessRegistrationNumber());
            if (merchantDetails.getTaxId() != null) 
                merchant.setTaxId(merchantDetails.getTaxId());
            if (merchantDetails.getAddress() != null) 
                merchant.setAddress(merchantDetails.getAddress());
            if (merchantDetails.getCity() != null) 
                merchant.setCity(merchantDetails.getCity());
            if (merchantDetails.getCountry() != null) 
                merchant.setCountry(merchantDetails.getCountry());
            
            return merchantRepository.save(merchant);
        }).orElseThrow(() -> new RuntimeException("Merchant not found with id: " + id));
    }

    public void verifyMerchant(UUID merchantId) {
        merchantRepository.findById(merchantId).ifPresent(merchant -> {
            merchant.setVerified(true);
            merchantRepository.save(merchant);
        });
    }

    // Admin methods
    public Admin createAdmin(Admin admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }

    public Optional<Admin> getAdminById(UUID id) {
        return adminRepository.findById(id);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public boolean isEmailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean isPhoneExists(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }

    // Count methods for dashboard
    public Long countAllUsers() {
        return userRepository.count();
    }

    public Long countAllMerchants() {
        return merchantRepository.count();
    }

    public Long countAllAdmins() {
        return adminRepository.count();
    }

    public Long countUsersRegisteredToday() {
        return userRepository.countUsersRegisteredToday();
    }

    public Long countVerifiedMerchants() {
        return merchantRepository.countVerifiedMerchants();
    }
}