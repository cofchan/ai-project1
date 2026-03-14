package com.registration.controller;

import com.registration.dto.*;
import com.registration.entity.User;
import com.registration.service.CaptchaService;
import com.registration.service.EmailService;
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
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174", "http://localhost:5175"})
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private TwoFactorAuthenticationService twoFAService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CaptchaService captchaService;

    /**
     * Register a new user
     */
    @GetMapping("/captcha")
    public ResponseEntity<CaptchaResponse> getCaptcha() {
        CaptchaService.Challenge challenge = captchaService.generateChallenge();
        return ResponseEntity.ok(CaptchaResponse.builder()
                .captchaToken(challenge.captchaToken())
                .captchaQuestion(challenge.captchaQuestion())
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequest request) {
        log.info("Register endpoint called for email: {}", request.getEmail());
        if (!captchaService.verify(request.getCaptchaToken(), request.getCaptchaAnswer())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(MessageResponse.builder().message("CAPTCHA verification failed").success(false).build());
        }
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

        if (!captchaService.verify(request.getCaptchaToken(), request.getCaptchaAnswer())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(MessageResponse.builder().message("CAPTCHA verification failed").success(false).build());
        }

        // First authenticate the user with credentials
        AuthResponse response = userService.authenticate(request.getEmail(), request.getPassword());
        
        // ensure flag is always set (builder may leave null)
        response.setRequiresTwoFA(false);

        // Check if user has 2FA enabled
        User user = userService.findUserByEmail(request.getEmail());
        if (user != null && Boolean.TRUE.equals(user.getIsTwoFAEnabled())) {
            // If 2FA is enabled, send a one-time code via email and postpone token
            userService.generateAndSendEmail2FACode(request.getEmail());

            response.setRequiresTwoFA(true);
            response.setToken(null); // Don't send token until 2FA is verified
            response.setMessage("2FA verification required; code sent to your email");
        } else {
            // Send login notification email for users without 2FA
            if (user != null) {
                emailService.sendLoginNotificationEmail(user);
            }
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
     * Redirect to OAuth2 login provider
     */
    @GetMapping("/login")
    public ResponseEntity<Void> oauth2LoginRedirect() {
        // Frontend should redirect to /oauth2/authorization/{provider}
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/oauth2/authorization/google") // or github, configurable
                .build();
    }

    /**
     * OAuth2 callback endpoint (Spring Security handles this, but you can add a custom handler if needed)
     */
    @GetMapping("/oauth2/callback")
    public ResponseEntity<?> oauth2Callback(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("OAuth2 authentication failed");
        }
        // You can extract user details from authentication.getPrincipal()
        return ResponseEntity.ok("OAuth2 login successful");
    }

    /**
     * Verify 2FA code during login
     */
    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verifyTwoFACode(@Valid @RequestBody TwoFALoginRequest request) {
        log.info("Verifying 2FA code for: {}", request.getEmail());
        AuthResponse response = userService.authenticateWith2FA(request.getEmail(), request.getCode());
        
        // Send login notification email after successful 2FA verification
        User user = userService.findUserByEmail(request.getEmail());
        if (user != null) {
            emailService.sendLoginNotificationEmail(user);
        }
        
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
        
        // Send login notification email after successful backup code verification
        User user = userService.findUserByEmail(request.getEmail());
        if (user != null) {
            emailService.sendLoginNotificationEmail(user);
        }
        
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
