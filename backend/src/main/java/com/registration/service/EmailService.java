package com.registration.service;

import com.registration.entity.EmailVerificationToken;
import com.registration.entity.PasswordResetToken;
import com.registration.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Send email verification email
     */
    public void sendVerificationEmail(User user, EmailVerificationToken token) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + token.getToken();

            String htmlContent = buildVerificationEmailContent(user.getFullName(), verificationUrl);

            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(user.getEmail());
                message.setSubject("Email Verification - User Registration System");
                message.setText(htmlContent);

                mailSender.send(message);
                log.info("Verification email sent successfully to: {}", user.getEmail());
            } else {
                log.warn("Mail sender not configured. Verification URL: {}", verificationUrl);
            }
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(User user, PasswordResetToken token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token.getToken();

            String htmlContent = buildPasswordResetEmailContent(user.getFullName(), resetUrl);

            if (mailSender != null) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(user.getEmail());
                message.setSubject("Password Reset - User Registration System");
                message.setText(htmlContent);

                mailSender.send(message);
                log.info("Password reset email sent successfully to: {}", user.getEmail());
            } else {
                log.warn("Mail sender not configured. Reset URL: {}", resetUrl);
            }
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Build verification email content
     */
    private String buildVerificationEmailContent(String fullName, String verificationUrl) {
        return "Hello " + fullName + ",\n\n" +
                "Thank you for registering! Please verify your email by clicking the link below:\n" +
                verificationUrl + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not register for this account, please ignore this email.\n\n" +
                "Best regards,\n" +
                "User Registration System";
    }

    /**
     * Build password reset email content
     */
    private String buildPasswordResetEmailContent(String fullName, String resetUrl) {
        return "Hello " + fullName + ",\n\n" +
                "You requested to reset your password. Please click the link below to reset it:\n" +
                resetUrl + "\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "User Registration System";
    }

        /**
         * Send a 6-digit code for email verification
         */
        public void sendVerificationCodeEmail(User user, String code) {
            try {
                String content = "Hello " + user.getFullName() + ",\n\n" +
                        "Your email verification code is: " + code + "\n\n" +
                        "Enter this code to verify your email.\n\n" +
                        "Best regards,\nUser Registration System";
                if (mailSender != null) {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(fromEmail);
                    message.setTo(user.getEmail());
                    message.setSubject("Email Verification Code - User Registration System");
                    message.setText(content);
                    mailSender.send(message);
                    log.info("Verification code email sent successfully to: {}", user.getEmail());
                } else {
                    log.warn("Mail sender not configured. Verification code: {}", code);
                }
            } catch (Exception e) {
                log.error("Failed to send verification code email to: {}", user.getEmail(), e);
                throw new RuntimeException("Failed to send verification code email", e);
            }
        }

        /**
         * Send a 6-digit code for password reset
         */
        public void sendPasswordResetCodeEmail(User user, String code) {
            try {
                String content = "Hello " + user.getFullName() + ",\n\n" +
                        "Your password reset code is: " + code + "\n\n" +
                        "Enter this code to reset your password.\n\n" +
                        "Best regards,\nUser Registration System";
                if (mailSender != null) {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(fromEmail);
                    message.setTo(user.getEmail());
                    message.setSubject("Password Reset Code - User Registration System");
                    message.setText(content);
                    mailSender.send(message);
                    log.info("Password reset code email sent successfully to: {}", user.getEmail());
                } else {
                    log.warn("Mail sender not configured. Password reset code: {}", code);
                }
            } catch (Exception e) {
                log.error("Failed to send password reset code email to: {}", user.getEmail(), e);
                throw new RuntimeException("Failed to send password reset code email", e);
            }
        }
}
