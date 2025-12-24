package com.htv.oauth2.resource;

import com.htv.oauth2.domain.AuthorizationCode;
import com.htv.oauth2.domain.Client;
import com.htv.oauth2.domain.User;
import com.htv.oauth2.dto.response.ErrorResponse;
import com.htv.oauth2.dto.response.TokenResponse;
import com.htv.oauth2.exception.auth.oauth2.InvalidRequestException;
import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;
import com.htv.oauth2.exception.auth.oauth2.UnsupportedGrantTypeException;
import com.htv.oauth2.service.auth.AuthenticationService;
import com.htv.oauth2.service.auth.AuthorizationService;
import com.htv.oauth2.service.client.ClientService;
import com.htv.oauth2.service.token.TokenService;
import com.htv.oauth2.util.StringUtil;
import com.htv.oauth2.util.ValidationUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
@Path("/oauth2/token")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class TokenResource {

    @Inject
    TokenService tokenService;

    @Inject
    AuthorizationService authorizationService;

    @Inject
    AuthenticationService authenticationService;

    @Inject
    ClientService clientService;

    /**
     * Token endpoint - Exchange authorization code for tokens
     * POST /oauth2/token
     */
    @POST
    @PermitAll
    public Response token(
            @FormParam("grant_type") String grantType,
            @FormParam("code") String code,
            @FormParam("redirect_uri") String redirectUri,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("refresh_token") String refreshToken,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("scope") String scope,
            @FormParam("code_verifier") String codeVerifier,
            @Context HttpHeaders headers) {

        try {
            // Validate grant type
            if (!ValidationUtil.isValidGrantType(grantType)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.unsupportedGrantType("Invalid grant type"))
                        .build();
            }

            // Extract client credentials from Basic Auth if not in form
            if (clientId == null && clientSecret == null) {
                String authHeader = headers.getHeaderString("Authorization");
                if (authHeader != null && authHeader.startsWith("Basic ")) {
                    // Parse Basic Auth (implement decoding)
                    // clientId = ...; clientSecret = ...;
                }
            }

            // Validate client
            Client client = clientService.validateClientCredentials(clientId, clientSecret);

            // Handle different grant types
            TokenResponse response = switch (grantType) {
                case "authorization_code" -> handleAuthorizationCodeGrant(
                        code, redirectUri, client, codeVerifier
                );
                case "refresh_token" -> handleRefreshTokenGrant(refreshToken, client);
                case "password" -> handlePasswordGrant(username, password, client, scope);
                case "client_credentials" -> handleClientCredentialsGrant(client, scope);
                default -> throw new UnsupportedGrantTypeException(grantType);
            };

            return Response.ok(response).build();

        } catch (OAuth2Exception e) {
            log.error("Token error: {}", e.getMessage(), e);
            ErrorResponse error = ErrorResponse.builder()
                    .error(e.getError())
                    .errorDescription(e.getErrorDescription())
                    .build();
            return Response.status(e.getHttpStatus()).entity(error).build();
        }
    }

    private TokenResponse handleAuthorizationCodeGrant(
            String code, String redirectUri, Client client, String codeVerifier) {

        if (code == null || redirectUri == null) {
            throw new InvalidRequestException("Missing code or redirect_uri");
        }

        // Validate and consume authorization code
        AuthorizationCode authCode = authorizationService.validateAndConsumeAuthorizationCode(
                code, client.getClientId(), redirectUri, codeVerifier
        );

        // Generate tokens
        return tokenService.generateTokens(
                authCode.getUser(),
                authCode.getClient(),
                authCode.getScopes()
        );
    }

    private TokenResponse handleRefreshTokenGrant(String refreshToken, Client client) {
        if (refreshToken == null) {
            throw new InvalidRequestException("Missing refresh_token");
        }

        return tokenService.refreshToken(refreshToken, client);
    }

    private TokenResponse handlePasswordGrant(
            String username, String password, Client client, String scope) {

        if (username == null || password == null) {
            throw new InvalidRequestException("Missing username or password");
        }

        // Validate credentials
        User user = authenticationService.validateCredentials(username, password);

        // Parse scopes
        Set<String> scopes = StringUtil.splitScopes(scope);
        if (scopes.isEmpty()) {
            scopes = Set.of("default");
        }

        return tokenService.generateTokens(user, client, scopes);
    }

    private TokenResponse handleClientCredentialsGrant(Client client, String scope) {
        // For client credentials, there's no user
        Set<String> scopes = StringUtil.splitScopes(scope);
        if (scopes.isEmpty()) {
            scopes = Set.of("default");
        }

        // Generate tokens without user
        throw new UnsupportedOperationException("Client credentials grant not yet implemented");
    }
}
