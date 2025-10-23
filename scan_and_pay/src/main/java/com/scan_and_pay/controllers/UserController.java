package com.scan_and_pay.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.scan_and_pay.models.Admin;
import com.scan_and_pay.models.Merchant;
import com.scan_and_pay.models.User;
import com.scan_and_pay.services.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // User endpoints
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (userService.isEmailExists(user.getEmail())) {
            return ResponseEntity.badRequest().body(null);
        }
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User userDetails) {
        try {
            User updatedUser = userService.updateUser(id, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/verify-email")
    public ResponseEntity<?> verifyEmail(@PathVariable UUID userId, @RequestParam String otpCode) {
        boolean verified = userService.verifyEmail(userId, otpCode);
        if (verified) {
            return ResponseEntity.ok().body("Email verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }

    // Merchant endpoints
    @PostMapping("/merchants")
    public ResponseEntity<Merchant> createMerchant(@RequestBody Merchant merchant) {
        if (userService.isEmailExists(merchant.getEmail())) {
            return ResponseEntity.badRequest().body(null);
        }
        Merchant createdMerchant = userService.createMerchant(merchant);
        return ResponseEntity.ok(createdMerchant);
    }

    @GetMapping("/merchants")
    public ResponseEntity<List<Merchant>> getAllMerchants() {
        List<Merchant> merchants = userService.getAllMerchants();
        return ResponseEntity.ok(merchants);
    }

    @GetMapping("/merchants/{id}")
    public ResponseEntity<Merchant> getMerchantById(@PathVariable UUID id) {
        return userService.getMerchantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/merchants/{id}")
    public ResponseEntity<Merchant> updateMerchant(@PathVariable UUID id, @RequestBody Merchant merchantDetails) {
        try {
            Merchant updatedMerchant = userService.updateMerchant(id, merchantDetails);
            return ResponseEntity.ok(updatedMerchant);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/merchants/{id}/verify")
    public ResponseEntity<Void> verifyMerchant(@PathVariable UUID id) {
        userService.verifyMerchant(id);
        return ResponseEntity.ok().build();
    }

    // Admin endpoints
    @PostMapping("/admins")
    public ResponseEntity<Admin> createAdmin(@RequestBody Admin admin) {
        if (userService.isEmailExists(admin.getEmail())) {
            return ResponseEntity.badRequest().body(null);
        }
        Admin createdAdmin = userService.createAdmin(admin);
        return ResponseEntity.ok(createdAdmin);
    }

    @GetMapping("/admins")
    public ResponseEntity<List<Admin>> getAllAdmins() {
        List<Admin> admins = userService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/admins/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable UUID id) {
        return userService.getAdminById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}