package com.htv.oauth2.resource;

import com.htv.oauth2.domain.Client;
import com.htv.oauth2.domain.User;
import com.htv.oauth2.dto.request.LoginRequest;
import com.htv.oauth2.dto.response.ErrorResponse;
import com.htv.oauth2.dto.response.LoginResponse;
import com.htv.oauth2.dto.response.TokenResponse;
import com.htv.oauth2.exception.MfaRequiredException;
import com.htv.oauth2.exception.OAuth2Exception;
import com.htv.oauth2.mapper.UserMapper;
import com.htv.oauth2.service.auth.AuthenticationService;
import com.htv.oauth2.service.token.TokenService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Set;

@Slf4j
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    @Inject
    AuthenticationService authService;

     @Inject
    TokenService tokenService;

    @Inject
    UserMapper userMapper;

    /**
     * Authenticate user (login)
     * POST /api/auth/login
     */
    @POST
    @Path("/login")
    @PermitAll
    public Response login(@Valid LoginRequest request) {
        try {
            User authenticatedUser = authService.authenticateUser(request);

            // --- GIẢ ĐỊNH LOGIC XÁC ĐỊNH CLIENT VÀ SCOPE ---
            // Trong luồng thực tế, Client ID và Scopes có thể được truyền trong request
            // hoặc được lấy từ cấu hình mặc định của ứng dụng client.

            // Giả định Client và Scopes Mặc định
            Client defaultClient = new Client(); // Lấy từ ClientRepository
            defaultClient.setClientId("default-client-id");
            defaultClient.setAccessTokenValidity(3600); // 1 giờ
            defaultClient.setRefreshTokenValidity(7200); // 2 giờ

            Set<String> defaultScopes = Set.of("profile", "email");

            // 1. Generate Tokens bằng TokenService
            TokenResponse tokenResponse = tokenService.generateTokens(
                    authenticatedUser,
                    defaultClient, // Sử dụng Client đã xác định
                    defaultScopes // Sử dụng Scopes đã xác định
            );

            // 2. Map TokenResponse và User thành LoginResponse
            LoginResponse response = LoginResponse.builder()
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .tokenType(tokenResponse.getTokenType())
                    .expiresIn(tokenResponse.getExpiresIn())
                    .scope(tokenResponse.getScope())
                    .mfaRequired(false) // Đăng nhập thành công, không yêu cầu MFA
                    .user(userMapper.toResponse(authenticatedUser)) // Map User Entity sang UserResponse DTO
                    .build();

            log.info("User {} logged in successfully with client {}.",
                    authenticatedUser.getUsername(), defaultClient.getClientId());

            return Response.ok(response).build();

        } catch (MfaRequiredException e) {
            // Xử lý trường hợp cần MFA (Giống như lần trước)
            log.warn("Login requires MFA for user {}.", request.getUsername());

            ErrorResponse error = ErrorResponse.builder()
                    .error(e.getError())
                    .errorDescription(e.getErrorDescription())
                    .status(400)
                    .timestamp(Instant.now())
                    .build();

            return Response.status(e.getHttpStatus()).entity(error).build();

        } catch (OAuth2Exception e) {
            log.warn("Login failed for user {}. Error: {}", request.getUsername(), e.getErrorDescription());

            ErrorResponse error = ErrorResponse.builder()
                    .error(e.getError())
                    .errorDescription(e.getErrorDescription())
                    .build();

            return Response.status(e.getHttpStatus()).entity(error).build();

        } catch (Exception e) {
            // Xử lý các lỗi không mong muốn khác (Lỗi Server 500)
            log.error("Unexpected error during login for user {}", request.getUsername(), e);
            ErrorResponse error = ErrorResponse.builder()
                    .error("internal_server_error")
                    .errorDescription("An unexpected error occurred.")
                    .build();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
}
