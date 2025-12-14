package com.htv.oauth2.service;

import com.htv.oauth2.domain.User;
import com.htv.oauth2.dto.request.RegisterRequest;
import com.htv.oauth2.exception.*;
import com.htv.oauth2.repository.UserRepository;
import com.htv.oauth2.service.security.PasswordService;
import com.htv.oauth2.service.user.UserService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// ============================================
// User Service Tests
// ============================================

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    @Inject
    UserService userService;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    PasswordService passwordService;

    @Test
    @Order(1)
    void testRegisterUser_Success() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Test@123")
                .confirmPassword("Test@123")
                .firstName("Test")
                .lastName("User")
                .build();

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordService.hashPassword(anyString())).thenReturn("hashed_password");

        // When
        var response = userService.registerUser(request);

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        verify(userRepository, times(1)).persist(any(User.class));
    }

    @Test
    @Order(2)
    void testRegisterUser_PasswordMismatch() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Test@123")
                .confirmPassword("DifferentPassword@123")
                .build();

        // When & Then
        assertThrows(PasswordMismatchException.class, () -> {
            userService.registerUser(request);
        });
    }

    @Test
    @Order(3)
    void testRegisterUser_UsernameExists() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .username("existing_user")
                .email("test@example.com")
                .password("Test@123")
                .confirmPassword("Test@123")
                .build();

        when(userRepository.existsByUsername("existing_user")).thenReturn(true);

        // When & Then
        assertThrows(UsernameAlreadyExistsException.class, () -> {
            userService.registerUser(request);
        });
    }

    @Test
    @Order(4)
    void testRegisterUser_EmailExists() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("existing@example.com")
                .password("Test@123")
                .confirmPassword("Test@123")
                .build();

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.registerUser(request);
        });
    }

    @Test
    @Order(5)
    void testFindById_Success() {
        // Given
        String userId = "test-user-id";
        User user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .build();

        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.of(user));

        // When
        var response = userService.findById(userId);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    @Order(6)
    void testFindById_NotFound() {
        // Given
        String userId = "non-existent-id";
        when(userRepository.findByIdOptional(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            userService.findById(userId);
        });
    }
}

// ============================================
// Token Service Tests
// ============================================

@QuarkusTest
class TokenServiceTest {

    @Inject
    com.htv.oauth2.service.token.TokenService tokenService;

    @InjectMock
    com.htv.oauth2.repository.AccessTokenRepository accessTokenRepository;

    @InjectMock
    com.htv.oauth2.repository.RefreshTokenRepository refreshTokenRepository;

    @InjectMock
    com.htv.oauth2.util.JwtUtil jwtUtil;

    @Test
    void testGenerateTokens_Success() {
        // Given
        User user = User.builder()
                .id("user-id")
                .username("testuser")
                .build();

        com.htv.oauth2.domain.Client client = com.htv.oauth2.domain.Client.builder()
                .clientId("test-client")
                .accessTokenValidity(3600)
                .refreshTokenValidity(86400)
                .build();

        when(jwtUtil.generateAccessToken(anyString(), anyString(), anySet()))
                .thenReturn("access_token_value");

        // When
        var response = tokenService.generateTokens(user, client, java.util.Set.of("read", "write"));

        // Then
        assertNotNull(response);
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(accessTokenRepository, times(1)).persist(Collections.singleton(any()));
        verify(refreshTokenRepository, times(1)).persist(Collections.singleton(any()));
    }
}

// ============================================
// Integration Tests - OAuth2 Flow
// ============================================

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OAuth2FlowIntegrationTest {

    @Inject
    com.htv.oauth2.service.auth.AuthorizationService authorizationService;

    @Inject
    com.htv.oauth2.service.token.TokenService tokenService;

    @Inject
    com.htv.oauth2.repository.ClientRepository clientRepository;

    @Inject
    com.htv.oauth2.repository.UserRepository userRepository;

    private User testUser;
    private com.htv.oauth2.domain.Client testClient;

    @BeforeEach
    void setup() {
        // Create test user
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("hashed_password")
                .enabled(true)
                .build();
        userRepository.persist(testUser);

        // Create test client
        testClient = com.htv.oauth2.domain.Client.builder()
                .clientId("test-client")
                .clientSecret("test-secret")
                .clientName("Test Client")
                .redirectUris(java.util.Set.of("http://localhost:8080/callback"))
                .grantTypes(java.util.Set.of("authorization_code", "refresh_token"))
                .scopes(java.util.Set.of("read", "write"))
                .accessTokenValidity(3600)
                .refreshTokenValidity(86400)
                .enabled(true)
                .build();
        clientRepository.persist(testClient);
    }

    @Test
    @Order(1)
    void testAuthorizationCodeFlow() {
        // Step 1: Create authorization code
        com.htv.oauth2.dto.request.AuthorizationRequest authRequest =
                com.htv.oauth2.dto.request.AuthorizationRequest.builder()
                        .responseType("code")
                        .clientId("test-client")
                        .redirectUri("http://localhost:8080/callback")
                        .scope("read write")
                        .state("random_state")
                        .build();

        var authResponse = authorizationService.createAuthorizationCode(
                authRequest, testUser, java.util.Set.of("read", "write")
        );

        assertNotNull(authResponse);
        assertNotNull(authResponse.getCode());
        assertEquals("random_state", authResponse.getState());

        // Step 2: Exchange code for tokens
        var authCode = authorizationService.validateAndConsumeAuthorizationCode(
                authResponse.getCode(),
                "test-client",
                "http://localhost:8080/callback",
                null
        );

        assertNotNull(authCode);
        assertTrue(authCode.getUsed());

        // Step 3: Generate tokens
        var tokenResponse = tokenService.generateTokens(
                authCode.getUser(),
                authCode.getClient(),
                authCode.getScopes()
        );

        assertNotNull(tokenResponse);
        assertNotNull(tokenResponse.getAccessToken());
        assertNotNull(tokenResponse.getRefreshToken());
        assertEquals("Bearer", tokenResponse.getTokenType());
    }
}

// ============================================
// REST API Integration Tests
// ============================================

@QuarkusTest
class OAuth2ResourceTest {

    @Test
    void testTokenEndpoint_InvalidGrantType() {
        io.restassured.RestAssured
                .given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "invalid_grant")
                .formParam("client_id", "test-client")
                .formParam("client_secret", "test-secret")
                .when()
                .post("/oauth2/token")
                .then()
                .statusCode(400)
                .body("error", org.hamcrest.Matchers.equalTo("unsupported_grant_type"));
    }

    @Test
    void testTokenEndpoint_MissingClientCredentials() {
        io.restassured.RestAssured
                .given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "authorization_code")
                .when()
                .post("/oauth2/token")
                .then()
                .statusCode(401);
    }

    @Test
    void testIntrospectionEndpoint_InvalidToken() {
        io.restassured.RestAssured
                .given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("token", "invalid_token")
                .formParam("client_id", "test-client")
                .formParam("client_secret", "test-secret")
                .when()
                .post("/oauth2/introspect")
                .then()
                .statusCode(200)
                .body("active", org.hamcrest.Matchers.equalTo(false));
    }

    @Test
    void testUserRegistration_Success() {
        var request = com.htv.oauth2.dto.request.RegisterRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("Password@123")
                .confirmPassword("Password@123")
                .firstName("New")
                .lastName("User")
                .build();

        io.restassured.RestAssured
                .given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(201)
                .body("username", org.hamcrest.Matchers.equalTo("newuser"))
                .body("email", org.hamcrest.Matchers.equalTo("newuser@example.com"));
    }

    @Test
    void testUserRegistration_PasswordMismatch() {
        var request = com.htv.oauth2.dto.request.RegisterRequest.builder()
                .username("newuser2")
                .email("newuser2@example.com")
                .password("Password@123")
                .confirmPassword("DifferentPassword@123")
                .build();

        io.restassured.RestAssured
                .given()
                .contentType("application/json")
                .body(request)
                .when()
                .post("/api/users/register")
                .then()
                .statusCode(400)
                .body("error", org.hamcrest.Matchers.equalTo("password_mismatch"));
    }
}