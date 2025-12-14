package com.htv.oauth2.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authorization_codes", indexes = {
        @Index(name = "idx_auth_code_code", columnList = "code"),
        @Index(name = "idx_auth_code_expires", columnList = "expires_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuthorizationCode extends PanacheEntityBase {

    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

    @Column(name = "code", unique = true, nullable = false, length = 255)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "redirect_uri", nullable = false, length = 500)
    private String redirectUri;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "authorization_code_scopes", joinColumns = @JoinColumn(name = "auth_code_id"))
    @Column(name = "scope")
    @Builder.Default
    private Set<String> scopes = new HashSet<>();

    @Column(name = "code_challenge", length = 255)
    private String codeChallenge;

    @Column(name = "code_challenge_method", length = 10)
    private String codeChallengeMethod;

    @Column(name = "used")
    @Builder.Default
    private Boolean used = false;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public boolean isValid() {
        return !used && !isExpired();
    }
}
