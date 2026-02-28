package com.registration.controller;

import com.registration.dto.*;
import com.registration.entity.User;
import com.registration.service.TwoFactorAuthenticationService;
import com.registration.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Base64;

@RestController
@RequestMapping("/auth")
@Validated
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private TwoFactorAuthenticationService twoFAService;

    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        log.info("Register endpoint called for email: {}", request.getEmail());
        RegistrationResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Verify email with token
     */
    @PostMapping("/verify-email")
    public ResponseEntity<UserResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        log.info("Verify email endpoint called");
        UserResponse response = userService.verifyEmail(request.getToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Login user
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login endpoint called for email: {}", request.getEmail());
        
        // First authenticate the user with credentials
        AuthResponse response = userService.authenticate(request.getEmail(), request.getPassword());
        
        // Check if user has 2FA enabled
        User user = userService.findUserByEmail(request.getEmail());
        if (user != null && user.getIsTwoFAEnabled()) {
            // If 2FA is enabled, don't return the token yet
            response.setRequiresTwoFA(true);
            response.setToken(null); // Don't send token until 2FA is verified
            response.setMessage("2FA verification required");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Request password reset
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("Forgot password endpoint called for email: {}", request.getEmail());
        userService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Password reset email has been sent to " + request.getEmail())
                .success(true)
                .build());
    }

    /**
     * Reset password with token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Reset password endpoint called");
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Password has been reset successfully")
                .success(true)
                .build());
    }

    /**
     * Get current user profile (protected endpoint)
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = authentication.getName();
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout user
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        log.info("Logout endpoint called");
        return ResponseEntity.ok(MessageResponse.builder()
                .message("Logged out successfully")
                .success(true)
                .build());
    }

    // ==================== 2FA ENDPOINTS ====================

    /**
     * Initiate 2FA setup
     */
    @PostMapping("/2fa/setup")
    public ResponseEntity<?> setupTwoFA(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        String secret = twoFAService.generateSecret();
        byte[] qrCodeImage = twoFAService.generateQRCode(email, secret);
        var backupCodes = twoFAService.generateBackupCodes();

        String qrCodeBase64 = Base64.getEncoder().encodeToString(qrCodeImage);

        TwoFASetupResponse response = TwoFASetupResponse.builder()
                .secret(secret)
                .qrCode(qrCodeImage)
                .qrCodeBase64("data:image/png;base64," + qrCodeBase64)
                .backupCodes(backupCodes.toArray(new String[0]))
                .message("Scan the QR code with your authenticator app and enter the code to confirm setup")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Confirm 2FA setup with code
     */
    @PostMapping("/2fa/confirm")
    public ResponseEntity<MessageResponse> confirmTwoFA(
            @Valid @RequestBody TwoFAConfirmRequest request,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();

        if (!twoFAService.verifyCode(request.getSecret(), request.getCode())) {
            return ResponseEntity.badRequest().body(MessageResponse.builder()
                    .message("Invalid 2FA code")
                    .success(false)
                    .build());
        }

        var backupCodes = twoFAService.generateBackupCodes();
        userService.enableTwoFA(email, request.getSecret(), backupCodes);

        return ResponseEntity.ok(MessageResponse.builder()
                .message("Two-factor authentication enabled successfully")
                .success(true)
                .build());
    }

    /**
     * Verify 2FA code during login
     */
    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verifyTwoFACode(@Valid @RequestBody TwoFALoginRequest request) {
        log.info("Verifying 2FA code for: {}", request.getEmail());
        AuthResponse response = userService.authenticateWith2FA(request.getEmail(), request.getCode());
        return ResponseEntity.ok(response);
    }

    /**
     * Disable 2FA
     */
    @PostMapping("/2fa/disable")
    public ResponseEntity<MessageResponse> disableTwoFA(
            @Valid @RequestBody TwoFADisableRequest request,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        userService.disableTwoFA(email, request.getPassword());

        return ResponseEntity.ok(MessageResponse.builder()
                .message("Two-factor authentication disabled")
                .success(true)
                .build());
    }

    /**
     * Verify backup code
     */
    @PostMapping("/2fa/backup-code")
    public ResponseEntity<?> verifyBackupCode(@Valid @RequestBody TwoFALoginRequest request) {
        log.info("Verifying backup code for: {}", request.getEmail());
        AuthResponse response = userService.authenticateWithBackupCode(request.getEmail(), request.getCode());
        return ResponseEntity.ok(response);
    }

    /**
     * Regenerate backup codes
     */
    @PostMapping("/2fa/backup-codes/regenerate")
    public ResponseEntity<TwoFASetupResponse> regenerateBackupCodes(
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        var backupCodes = twoFAService.generateBackupCodes();
        userService.updateBackupCodes(email, backupCodes);

        return ResponseEntity.ok(TwoFASetupResponse.builder()
                .backupCodes(backupCodes.toArray(new String[0]))
                .message("Backup codes regenerated successfully")
                .build());
    }
}
