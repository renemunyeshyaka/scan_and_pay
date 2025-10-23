package com.scan_and_pay.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.scan_and_pay.models.User;
import com.scan_and_pay.services.OTPService;
import com.scan_and_pay.services.UserService;
import com.scan_and_pay.utils.JwtTokenUtil;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private OTPService otpService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.getUserByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Send OTP for verification
            otpService.sendLoginOTP(user.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "OTP sent to your email");
            response.put("userId", user.getId());
            response.put("requiresOtp", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Invalid credentials", "message", e.getMessage())
            );
        }
    }

    @PostMapping("/verify-login")
    public ResponseEntity<?> verifyLogin(@RequestBody VerifyLoginRequest verifyRequest) {
        try {
            User user = userService.getUserByEmail(verifyRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean otpValid = otpService.verifyLoginOTP(user.getId(), verifyRequest.getOtpCode());

            if (otpValid) {
                // Generate token using UserDetails
                UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
                String token = jwtTokenUtil.generateToken(userDetails);
                
                // Create user response without sensitive data
                Map<String, Object> userResponse = createUserResponse(user);
                
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("user", userResponse);
                response.put("message", "Login successful");

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid OTP", "message", "The OTP code is invalid or expired")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Verification failed", "message", e.getMessage())
            );
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            if (userService.isEmailExists(registerRequest.getEmail())) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Email exists", "message", "Email already registered")
                );
            }

            User user = new User();
            user.setEmail(registerRequest.getEmail());
            user.setName(registerRequest.getName());
            user.setPhone(registerRequest.getPhone());
            user.setPassword(registerRequest.getPassword());
            user.setRole(registerRequest.getRole());

            User createdUser = userService.createUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully. Please verify your email.");
            response.put("userId", createdUser.getId());
            response.put("email", createdUser.getEmail());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Registration failed", "message", e.getMessage())
            );
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest forgotRequest) {
        try {
            User user = userService.getUserByEmail(forgotRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            otpService.sendPasswordResetOTP(user.getEmail());
            
            return ResponseEntity.ok(
                Map.of("message", "Password reset OTP sent to your email")
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "User not found", "message", "No account found with this email")
            );
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest resetRequest) {
        try {
            User user = userService.getUserByEmail(resetRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean otpValid = otpService.verifyOTP(user.getId(), resetRequest.getOtpCode(), "PASSWORD_RESET");

            if (otpValid) {
                // Update password through service
                user.setPassword(resetRequest.getNewPassword());
                userService.updateUser(user.getId(), user);
                
                return ResponseEntity.ok(
                    Map.of("message", "Password reset successfully")
                );
            } else {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid OTP", "message", "The OTP code is invalid or expired")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Password reset failed", "message", e.getMessage())
            );
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid token", "message", "Authorization header is missing or invalid")
                );
            }

            String token = authHeader.substring(7);
            if (jwtTokenUtil.canTokenBeRefreshed(token)) {
                String refreshedToken = jwtTokenUtil.refreshToken(token);
                return ResponseEntity.ok(Map.of("token", refreshedToken));
            } else {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Token expired", "message", "Token cannot be refreshed")
                );
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                Map.of("error", "Token refresh failed", "message", e.getMessage())
            );
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Invalid token", "message", "Authorization header is missing")
                );
            }

            String token = authHeader.substring(7);
            if (!jwtTokenUtil.validateToken(token)) {
                return ResponseEntity.status(401).body(
                    Map.of("error", "Invalid token", "message", "Token is invalid or expired")
                );
            }

            String email = jwtTokenUtil.getEmailFromToken(token);
            User user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> userResponse = createUserResponse(user);
            return ResponseEntity.ok(userResponse);

        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                Map.of("error", "Authentication failed", "message", e.getMessage())
            );
        }
    }

    // Helper method to create user response without sensitive data
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("name", user.getName());
        userResponse.put("email", user.getEmail());
        userResponse.put("phone", user.getPhone());
        userResponse.put("role", user.getRole());
        userResponse.put("enabled", user.isEnabled());
        userResponse.put("emailVerified", user.isEmailVerified());
        userResponse.put("phoneVerified", user.isPhoneVerified());
        userResponse.put("createdAt", user.getCreatedAt());
        return userResponse;
    }

    // Request DTOs
    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class VerifyLoginRequest {
        private String email;
        private String otpCode;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOtpCode() { return otpCode; }
        public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
    }

    public static class RegisterRequest {
        private String name;
        private String email;
        private String phone;
        private String password;
        private String role;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class ForgotPasswordRequest {
        private String email;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ResetPasswordRequest {
        private String email;
        private String otpCode;
        private String newPassword;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getOtpCode() { return otpCode; }
        public void setOtpCode(String otpCode) { this.otpCode = otpCode; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}