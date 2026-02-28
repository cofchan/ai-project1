package com.registration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = {"oauth2_provider", "oauth2_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(length = 255)
    private String fullName;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    @Column(length = 50)
    private String oauth2Provider;

    @Column(length = 255)
    private String oauth2Id;

    @Column(name = "is_two_fa_enabled", nullable = false)
    private Boolean isTwoFAEnabled = false;

    @Column(name = "two_fa_secret", length = 255)
    private String twoFASecret;

    @Column(name = "two_fa_backup_codes", columnDefinition = "TEXT")
    private String twoFABackupCodes; // JSON array of backup codes

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
