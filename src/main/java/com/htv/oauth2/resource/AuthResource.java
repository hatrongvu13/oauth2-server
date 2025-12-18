package com.htv.oauth2.resource;

import com.htv.oauth2.domain.Client;
import com.htv.oauth2.domain.User;
import com.htv.oauth2.dto.request.EnableMfaRequest;
import com.htv.oauth2.dto.request.LoginRequest;
import com.htv.oauth2.dto.request.MfaVerifyRequest;
import com.htv.oauth2.dto.response.*;
import com.htv.oauth2.exception.*;
import com.htv.oauth2.mapper.UserMapper;
import com.htv.oauth2.service.auth.AuthenticationService;
import com.htv.oauth2.service.mfa.MfaService;
import com.htv.oauth2.service.token.TokenService;
import com.htv.oauth2.service.user.UserService;
import io.quarkus.security.Authenticated;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Instant;
import java.util.Map;
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
    UserService userService;

    @Inject
    MfaService mfaService;

    @Inject
    JsonWebToken jwt;

    @Inject
    UserMapper userMapper;

    private Response buildErrorResponse(OAuth2Exception e, Response.Status defaultStatus) {

        Map<String, Object> info = null;

        if (e instanceof MfaRequiredException) {
            info = Map.of("mfa_token", ((MfaRequiredException) e).getMfaToken());
        } else if (e.getAdditionalInfo() != null) {
            info = Map.of("additional_data", e.getAdditionalInfo());
        }

        ErrorResponse error = ErrorResponse.builder()
                .error(e.getError())
                .errorDescription(e.getErrorDescription())
                .status(e.getHttpStatus())
                .timestamp(Instant.now())
                .additionalInfo(info)
                .build();

        return Response.status(e.getHttpStatus()).entity(error).build();
    }

    private Response buildInternalServerError(Exception e) {
        log.error("Unexpected error occurred: ", e);
        ErrorResponse error = ErrorResponse.builder()
                .error("internal_server_error")
                .errorDescription("An unexpected error occurred. Please contact support.")
                .status(500)
                .timestamp(Instant.now())
                .build();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }


    /**
     * Authenticate user (login)
     * POST /api/auth/login
     */
    @POST
    @Path("/login")
    @PermitAll
    public Response login(
            @Valid LoginRequest request,
            @Context RoutingContext routingContext, // Để lấy IP
            @Context HttpHeaders headers // Để lấy User Agent
    ) {
        String ipAddress = routingContext.request().remoteAddress().hostAddress();
        String userAgent = headers.getHeaderString(HttpHeaders.USER_AGENT);

        try {
            // 1. Xác thực người dùng (truyền thêm IP và User Agent)
            User authenticatedUser = authService.authenticateUser(request, ipAddress, userAgent);

            // --- GIẢ ĐỊNH LOGIC XÁC ĐỊNH CLIENT VÀ SCOPE ---
            // Giả định Client và Scopes Mặc định
            Client defaultClient = createDefaultClient();
            Set<String> defaultScopes = Set.of("profile", "email");

            // 2. Generate Tokens
            TokenResponse tokenResponse = tokenService.generateTokens(
                    authenticatedUser, defaultClient, defaultScopes
            );

            // 3. Map TokenResponse và User thành LoginResponse
            LoginResponse response = LoginResponse.builder()
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .tokenType(tokenResponse.getTokenType())
                    .expiresIn(tokenResponse.getExpiresIn())
                    .scope(tokenResponse.getScope())
                    .mfaRequired(false)
                    .user(userMapper.toResponse(authenticatedUser))
                    .build();

            log.info("User {} logged in successfully.", authenticatedUser.getUsername());

            return Response.ok(response).build();

        } catch (MfaRequiredException e) {
            // Xử lý trường hợp cần MFA (Trả về 401 Unauthorized và mfa_token)
            log.warn("Login requires MFA for user {}. Token: {}", request.getUsername(), e.getAdditionalInfo());
            return buildErrorResponse(e, Response.Status.UNAUTHORIZED); // Sử dụng hàm tiện ích

        } catch (OAuth2Exception e) {
            // Xử lý các Exception nghiệp vụ khác (InvalidCredentials, RateLimitExceeded, AccountLocked, v.v.)
            log.warn("Login failed for user {}. Error: {}", request.getUsername(), e.getErrorDescription());
            return buildErrorResponse(e, Response.Status.BAD_REQUEST);

        } catch (Exception e) {
            // Xử lý lỗi hệ thống 500
            return buildInternalServerError(e);
        }
    }

    /**
     * Verify MFA code and complete login
     * POST /api/auth/mfa/verify
     */
    @POST
    @Path("/mfa/verify")
    @PermitAll
    public Response verifyMfa(
            @Valid MfaVerifyRequest request,
            @Context RoutingContext routingContext,
            @Context HttpHeaders headers
    ) {
        String ipAddress = routingContext.request().remoteAddress().hostAddress();
        String userAgent = headers.getHeaderString(HttpHeaders.USER_AGENT);

        try {
            // 1. Xác thực MFA session token và code
            User authenticatedUser = authService.verifyMfaSession(
                    request.getMfaToken(),
                    request.getMfaCode(),
                    ipAddress,
                    userAgent
            );

            // --- LOGIC TẠO TOKEN TƯƠNG TỰ LOGIN ---
            Client defaultClient = new Client();
            defaultClient.setClientId("default-client-id");
            defaultClient.setAccessTokenValidity(3600);
            defaultClient.setRefreshTokenValidity(7200);
            Set<String> defaultScopes = Set.of("profile", "email");

            // 2. Generate Tokens
            TokenResponse tokenResponse = tokenService.generateTokens(
                    authenticatedUser, defaultClient, defaultScopes
            );

            // 3. Map TokenResponse và User thành LoginResponse
            LoginResponse response = LoginResponse.builder()
                    .accessToken(tokenResponse.getAccessToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .tokenType(tokenResponse.getTokenType())
                    .expiresIn(tokenResponse.getExpiresIn())
                    .scope(tokenResponse.getScope())
                    .mfaRequired(false)
                    .user(userMapper.toResponse(authenticatedUser))
                    .build();

            log.info("User {} successfully completed MFA.", authenticatedUser.getUsername());

            return Response.ok(response).build();

        } catch (OAuth2Exception e) {
            // Xử lý InvalidMfaCodeException, InvalidTokenException
            log.warn("MFA verification failed. Error: {}", e.getErrorDescription());
            return buildErrorResponse(e, Response.Status.UNAUTHORIZED);
        } catch (Exception e) {
            return buildInternalServerError(e);
        }
    }

    @POST
    @Path("/mfa/enable")
    // Giả định endpoint này yêu cầu người dùng ĐÃ đăng nhập (Authenticated)
    // Nếu bạn muốn cho phép kích hoạt ngay sau đăng ký mà chưa đăng nhập, bạn cần dùng token tạm thời
    // Tạm thời, dùng SecurityContext để lấy User sau khi đăng nhập cơ bản.
    // Nếu bạn muốn client kích hoạt ngay sau đăng ký, họ cần phải login trước.
    // Hoặc bạn phải định nghĩa một cơ chế xác thực tạm thời khác cho API này.
    // Giả định: Người dùng đã login (có Access Token) hoặc đây là luồng Admin/User Profile.
    // Nếu đây là luồng đăng ký, thường client sẽ lưu secret và gọi API này.
    // Tạm thời, đặt PermitAll để cho phép truy cập sau đăng ký nếu bạn không yêu cầu login ngay.
    @Authenticated
    public Response enableMfa(@Valid EnableMfaRequest request) {
        try {
            // Lấy người dùng hiện tại (Giả định Auth mechanism đã xác thực UserID và đặt vào UserContext)
            String userId = jwt.getSubject();

            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            // 2. Kích hoạt MFA chính thức và xóa mã MFA tạm thời khỏi bộ nhớ/DB
            userService.enableMfa(userId, request);

            log.info("MFA successfully enabled for user {}", userId);
            return Response.ok(SuccessResponse.of("MFA successfully enabled.")).build();

        } catch (OAuth2Exception e) {
            return buildErrorResponse(e, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            return buildInternalServerError(e);
        }
    }

    /**
     * Disable MFA for the current authenticated user
     * POST /api/auth/mfa/disable
     */
    @POST
    @Path("/mfa/disable")
    @Authenticated
    public Response disableMfa() {
        try {
            // 1. Lấy userId từ JWT Subject
            String userId = jwt.getSubject();

            if (userId == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            log.info("Request to disable MFA for user: {}", userId);

            // 2. Gọi MfaService để xóa cấu hình MFA (Secret key, backup codes)
            mfaService.disableMfa(userId);

            // 3. Cập nhật trạng thái mfaEnabled = false trong bảng User thông qua UserService
            userService.disableMfa(userId);

            log.info("MFA successfully disabled for user {}", userId);
            return Response.ok(SuccessResponse.of("MFA has been successfully disabled.")).build();

        } catch (ResourceNotFoundException e) {
            return buildErrorResponse(e, Response.Status.NOT_FOUND);
        } catch (OAuth2Exception e) {
            return buildErrorResponse(e, Response.Status.BAD_REQUEST);
        } catch (Exception e) {
            return buildInternalServerError(e);
        }
    }

    private Client createDefaultClient() {
        Client client = new Client();
        client.setClientId("default-client-id");
        client.setAccessTokenValidity(3600);
        client.setRefreshTokenValidity(7200);
        return client;
    }
}
