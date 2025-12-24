package com.htv.oauth2.service.auth;

import com.htv.oauth2.domain.*;
import com.htv.oauth2.dto.auth.AuthorizationRequest;
import com.htv.oauth2.dto.response.AuthorizationResponse;
import com.htv.oauth2.exception.auth.oauth2.CodeChallengeMismatchException;
import com.htv.oauth2.exception.auth.oauth2.InvalidClientException;
import com.htv.oauth2.exception.auth.oauth2.InvalidCodeVerifierException;
import com.htv.oauth2.exception.auth.oauth2.UnsupportedResponseTypeException;
import com.htv.oauth2.exception.auth.token.ExpiredAuthorizationCodeException;
import com.htv.oauth2.exception.auth.token.InvalidAuthorizationCodeException;
import com.htv.oauth2.repository.*;
import com.htv.oauth2.service.client.ClientService;
import com.htv.oauth2.util.CryptoUtil;
import com.htv.oauth2.util.DateTimeUtil;
import com.htv.oauth2.util.ValidationUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Set;

@Slf4j
@ApplicationScoped
public class AuthorizationService {

    @Inject
    ClientRepository clientRepository;

    @Inject
    AuthorizationCodeRepository authCodeRepository;

    @Inject
    UserConsentRepository consentRepository;

    @Inject
    ClientService clientService;

    @ConfigProperty(name = "oauth2.authorization-code.validity", defaultValue = "300")
    Integer authCodeValidity; // 5 minutes

    /**
     * Create authorization code
     */
    @Transactional
    public AuthorizationResponse createAuthorizationCode(
            AuthorizationRequest request,
            User user,
            Set<String> approvedScopes) {

        log.info("Creating authorization code for user {} and client {}",
                user.getId(), request.getClientId());

        // Validate response type
        if (!"code".equals(request.getResponseType())) {
            throw new UnsupportedResponseTypeException(request.getResponseType());
        }

        // Find and validate client
        Client client = clientRepository.findByClientId(request.getClientId())
                .orElseThrow(() -> new InvalidClientException("Client not found"));

        if (!client.getEnabled()) {
            throw new InvalidClientException("Client is disabled");
        }

        // Validate redirect URI
        clientService.validateRedirectUri(client, request.getRedirectUri());

        // Validate grant type
        clientService.validateGrantType(client, "authorization_code");

        // Validate scopes
        if (!ValidationUtil.areScopesAllowed(approvedScopes, client.getScopes())) {
            throw new InvalidCodeVerifierException.InvalidScopeException("Requested scopes not allowed for this client");
        }

        // Generate authorization code
        String code = CryptoUtil.generateAuthorizationCode();

        AuthorizationCode authCode = AuthorizationCode.builder()
                .code(code)
                .client(client)
                .user(user)
                .redirectUri(request.getRedirectUri())
                .scopes(approvedScopes)
                .codeChallenge(request.getCodeChallenge())
                .codeChallengeMethod(request.getCodeChallengeMethod())
                .expiresAt(DateTimeUtil.expiresAt(authCodeValidity))
                .build();

        authCodeRepository.persist(authCode);

        // Save user consent if remember is enabled
        if (client.getAutoApprove() || shouldRememberConsent(user, client)) {
            saveUserConsent(user, client, approvedScopes);
        }

        log.info("Authorization code created successfully");

        return AuthorizationResponse.builder()
                .code(code)
                .state(request.getState())
                .redirectUri(request.getRedirectUri())
                .expiresIn(authCodeValidity.longValue())
                .build();
    }

    /**
     * Exchange authorization code for tokens
     */
    @Transactional
    public AuthorizationCode validateAndConsumeAuthorizationCode(
            String code,
            String clientId,
            String redirectUri,
            String codeVerifier) {

        log.info("Validating authorization code for client {}", clientId);

        // Find authorization code
        AuthorizationCode authCode = authCodeRepository.findByCode(code)
                .orElseThrow(() -> new InvalidAuthorizationCodeException("Authorization code not found"));

        // Validate not used
        if (authCode.getUsed()) {
            throw new InvalidAuthorizationCodeException("Authorization code already used");
        }

        // Validate not expired
        if (authCode.isExpired()) {
            throw new ExpiredAuthorizationCodeException("Authorization code has expired");
        }

        // Validate client
        if (!authCode.getClient().getClientId().equals(clientId)) {
            throw new InvalidClientException("Authorization code does not belong to this client");
        }

        // Validate redirect URI
        if (!authCode.getRedirectUri().equals(redirectUri)) {
            throw new InvalidCodeVerifierException.InvalidRedirectUriException("Redirect URI does not match");
        }

        // Validate PKCE if present
        if (authCode.getCodeChallenge() != null) {
            if (codeVerifier == null) {
                throw new InvalidCodeVerifierException("Code verifier required for PKCE");
            }

            if (!CryptoUtil.verifyPkceChallenge(
                    codeVerifier,
                    authCode.getCodeChallenge(),
                    authCode.getCodeChallengeMethod())) {
                throw new CodeChallengeMismatchException();
            }
        }

        // Mark as used
        authCode.setUsed(true);
        authCodeRepository.persist(authCode);

        log.info("Authorization code validated and consumed");
        return authCode;
    }

    /**
     * Get client by client ID
     */
    public Client getClientByClientId(String clientId) {
        return clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new InvalidClientException("Client not found: " + clientId));
    }

    /**
     * Check if user needs consent
     */
    public boolean needsConsent(User user, Client client, Set<String> requestedScopes) {
        if (client.getAutoApprove()) {
            return false;
        }

        return consentRepository.findByUserAndClient(user.getId(), client.getClientId())
                .map(consent -> !consent.getApprovedScopes().containsAll(requestedScopes))
                .orElse(true);
    }

    /**
     * Save user consent
     */
    @Transactional
    public void saveUserConsent(User user, Client client, Set<String> approvedScopes) {
        UserConsent consent = consentRepository
                .findByUserAndClient(user.getId(), client.getClientId())
                .orElse(UserConsent.builder()
                        .user(user)
                        .client(client)
                        .build());

        consent.setApprovedScopes(approvedScopes);
        consentRepository.persist(consent);

        log.info("User consent saved for user {} and client {}", user.getId(), client.getClientId());
    }

    /**
     * Revoke user consent
     */
    @Transactional
    public void revokeConsent(String userId, String clientId) {
        consentRepository.deleteByUserAndClient(userId, clientId);
        log.info("User consent revoked for user {} and client {}", userId, clientId);
    }

    private boolean shouldRememberConsent(User user, Client client) {
        // Add logic to check if user wants to remember consent
        return false;
    }

    /**
     * Clean up expired authorization codes
     */
    @Transactional
    public void cleanupExpiredAuthorizationCodes() {
        log.info("Cleaning up expired authorization codes");
        long deleted = authCodeRepository.deleteExpired();
        log.info("Deleted {} expired authorization codes", deleted);
    }
}