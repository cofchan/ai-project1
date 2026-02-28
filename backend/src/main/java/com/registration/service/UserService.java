package com.registration.service;

import com.registration.config.JwtTokenProvider;
import com.registration.dto.*;
import com.registration.entity.EmailVerificationToken;
import com.registration.entity.PasswordResetToken;
import com.registration.entity.User;
import com.registration.exception.InvalidTokenException;
import com.registration.exception.UserAlreadyExistsException;
import com.registration.exception.UserNotFoundException;
import com.registration.repository.EmailVerificationTokenRepository;
import com.registration.repository.PasswordResetTokenRepository;
import com.registration.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private TwoFactorAuthenticationService twoFAService;

    @Value("${app.email-verification-expiry-hours:24}")
    private int emailVerificationExpiryHours;

    @Value("${app.password-reset-expiry-minutes:60}")
    private int passwordResetExpiryMinutes;

    /**
     * Register a new user
     */
    public RegistrationResponse registerUser(RegistrationRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("User registration failed - email already exists: {}", request.getEmail());
            throw new UserAlreadyExistsException("Email is already registered");
        }

        // Validate password and password confirmation match
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new InvalidTokenException("Passwords do not match");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .isEmailVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully with id: {}", user.getId());

        // Generate email verification token
        EmailVerificationToken verificationToken = generateEmailVerificationToken(user);
        emailService.sendVerificationEmail(user, verificationToken);
        log.info("Verification email sent to: {}", user.getEmail());

        return RegistrationResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isEmailVerified(user.getIsEmailVerified())
                .message("User registered successfully. Please verify your email.")
                .build();
    }

    /**
     * Verify email with token
     */
    public UserResponse verifyEmail(String token) {
        log.info("Verifying email with token");

        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Email verification failed - invalid token");
                    return new InvalidTokenException("Invalid verification token");
                });

        if (verificationToken.isExpired()) {
            log.warn("Email verification failed - token expired");
            // Delete expired token
            emailVerificationTokenRepository.delete(verificationToken);
            throw new InvalidTokenException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setIsEmailVerified(true);
        user = userRepository.save(user);
        log.info("Email verified successfully for user: {}", user.getEmail());

        // Delete used token
        emailVerificationTokenRepository.delete(verificationToken);

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isEmailVerified(user.getIsEmailVerified())
                .build();
    }

    /**
     * Generate password reset token and send email
     */
    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Password reset failed - user not found: {}", email);
                    return new UserNotFoundException("User with email " + email + " not found");
                });

        // Generate password reset token
        PasswordResetToken resetToken = generatePasswordResetToken(user);
        emailService.sendPasswordResetEmail(user, resetToken);
        log.info("Password reset email sent to: {}", user.getEmail());
    }

    /**
     * Reset password with token
     */
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token");

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.warn("Password reset failed - invalid token");
                    return new InvalidTokenException("Invalid reset token");
                });

        if (resetToken.isExpired()) {
            log.warn("Password reset failed - token expired");
            throw new InvalidTokenException("Reset token has expired");
        }

        if (resetToken.getUsed()) {
            log.warn("Password reset failed - token already used");
            throw new InvalidTokenException("Reset token has already been used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset successfully for user: {}", user.getEmail());

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    /**
     * Authenticate user with email and password
     */
    public AuthResponse authenticate(String email, String password) {
        log.info("Authenticating user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Authentication failed - user not found: {}", email);
                    return new UserNotFoundException("Invalid email or password");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Authentication failed - invalid password for user: {}", email);
            throw new UserNotFoundException("Invalid email or password");
        }

        if (!user.getIsEmailVerified()) {
            log.warn("Authentication failed - email not verified for user: {}", email);
            throw new InvalidTokenException("Email not verified. Please verify your email first.");
        }

        log.info("User authenticated successfully: {}", email);

        String token = jwtTokenProvider.generateToken(email);

        return AuthResponse.builder()
                .token(token)
                .user(convertToUserResponse(user))
                .message("Authentication successful")
                .build();
    }

    /**
     * Find user by email (returns User entity)
     */
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
    }

    /**
     * Find user by email (returns UserResponse DTO)
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));
        return convertToUserResponse(user);
    }

    /**
     * Find or create user from OAuth2 (Social login)
     */
    public User findOrCreateOAuth2User(String email, String fullName, String provider, String oauth2Id) {
        log.info("Finding or creating OAuth2 user: {}", email);

        return userRepository.findByOauth2ProviderAndOauth2Id(provider, oauth2Id)
                .orElseGet(() -> {
                    // Check if email already exists
                    if (userRepository.existsByEmail(email)) {
                        throw new UserAlreadyExistsException("Email " + email + " is already registered");
                    }

                    // Create new OAuth2 user
                    User newUser = User.builder()
                            .email(email)
                            .fullName(fullName)
                            .isEmailVerified(true) // OAuth2 users are automatically email verified
                            .oauth2Provider(provider)
                            .oauth2Id(oauth2Id)
                            .build();

                    newUser = userRepository.save(newUser);
                    log.info("New OAuth2 user created with id: {}", newUser.getId());
                    return newUser;
                });
    }

    /**
     * Generate email verification token
     */
    private EmailVerificationToken generateEmailVerificationToken(User user) {
        // Delete existing token if any
        emailVerificationTokenRepository.deleteByUserId(user.getId());

        LocalDateTime expiryTime = LocalDateTime.now().plusHours(emailVerificationExpiryHours);
        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .expiryTime(expiryTime)
                .build();

        return emailVerificationTokenRepository.save(token);
    }

    /**
     * Generate password reset token
     */
    private PasswordResetToken generatePasswordResetToken(User user) {
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(passwordResetExpiryMinutes);
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .expiryTime(expiryTime)
                .used(false)
                .build();

        return passwordResetTokenRepository.save(token);
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isEmailVerified(user.getIsEmailVerified())
                .oauth2Provider(user.getOauth2Provider())
                .build();
    }

    /**
     * Enable two-factor authentication
     */
    public void enableTwoFA(String email, String secret, List<String> backupCodes) {
        log.info("Enabling 2FA for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));

        user.setIsTwoFAEnabled(true);
        user.setTwoFASecret(secret);
        user.setTwoFABackupCodes(twoFAService.backupCodesToJson(backupCodes));
        userRepository.save(user);
        log.info("2FA enabled for user: {}", email);
    }

    /**
     * Authenticate user with 2FA code
     */
    public AuthResponse authenticateWith2FA(String email, String code) {
        log.info("Authenticating user with 2FA: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Invalid email or code"));

        if (!user.getIsTwoFAEnabled()) {
            throw new InvalidTokenException("2FA not enabled for this user");
        }

        if (!twoFAService.verifyCode(user.getTwoFASecret(), code)) {
            throw new InvalidTokenException("Invalid 2FA code");
        }

        log.info("User authenticated with 2FA: {}", email);
        String token = jwtTokenProvider.generateToken(email);

        return AuthResponse.builder()
                .token(token)
                .user(convertToUserResponse(user))
                .message("Authentication successful with 2FA")
                .build();
    }

    /**
     * Authenticate user with backup code
     */
    public AuthResponse authenticateWithBackupCode(String email, String code) {
        log.info("Authenticating user with backup code: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Invalid email or backup code"));

        if (!user.getIsTwoFAEnabled()) {
            throw new InvalidTokenException("2FA not enabled for this user");
        }

        if (!twoFAService.verifyBackupCode(user.getTwoFABackupCodes(), code)) {
            throw new InvalidTokenException("Invalid backup code");
        }

        // Remove used backup code
        String updatedCodes = twoFAService.removeBackupCode(user.getTwoFABackupCodes(), code);
        user.setTwoFABackupCodes(updatedCodes);
        userRepository.save(user);
        log.info("User authenticated with backup code: {}", email);

        String token = jwtTokenProvider.generateToken(email);

        return AuthResponse.builder()
                .token(token)
                .user(convertToUserResponse(user))
                .message("Authentication successful with backup code")
                .build();
    }

    /**
     * Disable two-factor authentication
     */
    public void disableTwoFA(String email, String password) {
        log.info("Disabling 2FA for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidTokenException("Invalid password");
        }

        user.setIsTwoFAEnabled(false);
        user.setTwoFASecret(null);
        user.setTwoFABackupCodes(null);
        userRepository.save(user);
        log.info("2FA disabled for user: {}", email);
    }

    /**
     * Update backup codes
     */
    public void updateBackupCodes(String email, List<String> backupCodes) {
        log.info("Updating backup codes for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email " + email + " not found"));

        user.setTwoFABackupCodes(twoFAService.backupCodesToJson(backupCodes));
        userRepository.save(user);
        log.info("Backup codes updated for user: {}", email);
    }
}
