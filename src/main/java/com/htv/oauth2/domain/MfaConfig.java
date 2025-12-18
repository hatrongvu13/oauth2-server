package com.htv.oauth2.domain;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_mfa_config")
public class MfaConfig {

    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    private String id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "secret_key", nullable = false)
    private String secretKey;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = false;

    @Column(name = "backup_codes")
    private String backupCodes; // JSON array of backup codes

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
