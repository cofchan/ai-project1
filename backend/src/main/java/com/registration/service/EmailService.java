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
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

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
     * Send email verification email using a template file
     */
    public void sendVerificationEmail(User user, EmailVerificationToken token) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + token.getToken();
            String htmlContent = buildVerificationEmailContent(user.getFullName(), verificationUrl, token.getToken());

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
     * Send password reset email using a template file
     */
    public void sendPasswordResetEmail(User user, PasswordResetToken token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token.getToken();
            String htmlContent = buildPasswordResetEmailContent(user.getFullName(), resetUrl, token.getToken());

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
     * Load a template file from classpath under /email
     */
    private String loadTemplate(String filename) {
        try {
            ClassPathResource resource = new ClassPathResource("email/" + filename);
            try (InputStream in = resource.getInputStream()) {
                return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("Failed to load email template {}", filename, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Build verification email content
     */
    private String buildVerificationEmailContent(String fullName, String verificationUrl, String token) {
        String template = loadTemplate("verification_email.txt");
        return template
                .replace("{{fullName}}", fullName)
                .replace("{{verificationUrl}}", verificationUrl)
                .replace("{{token}}", token);
    }

    /**
     * Build password reset email content
     */
    private String buildPasswordResetEmailContent(String fullName, String resetUrl, String token) {
        String template = loadTemplate("password_reset_email.txt");
        return template
                .replace("{{fullName}}", fullName)
                .replace("{{resetUrl}}", resetUrl)
                .replace("{{token}}", token);
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
