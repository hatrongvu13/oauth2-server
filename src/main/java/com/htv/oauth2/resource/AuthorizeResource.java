package com.htv.oauth2.resource;

import com.htv.oauth2.domain.*;
import com.htv.oauth2.dto.request.*;
import com.htv.oauth2.dto.response.*;
import com.htv.oauth2.exception.*;
import com.htv.oauth2.service.auth.*;
import com.htv.oauth2.util.HttpUtil;
import com.htv.oauth2.util.StringUtil;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

@Slf4j
@Path("/oauth2/authorize")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class AuthorizeResource {

    @Inject
    AuthorizationService authorizationService;

    @Inject
    AuthenticationService authenticationService;

    @Context
    SecurityContext securityContext;

    /**
     * Authorization endpoint - Step 1: Show login/consent page
     * GET /oauth2/authorize?response_type=code&client_id=...&redirect_uri=...&scope=...&state=...
     */
    @GET
    @PermitAll
    public Response authorize(
            @QueryParam("response_type") String responseType,
            @QueryParam("client_id") String clientId,
            @QueryParam("redirect_uri") String redirectUri,
            @QueryParam("scope") String scope,
            @QueryParam("state") String state,
            @QueryParam("code_challenge") String codeChallenge,
            @QueryParam("code_challenge_method") String codeChallengeMethod,
            @Context UriInfo uriInfo) {

        try {
            // Validate parameters
            if (StringUtil.isEmpty(clientId) || StringUtil.isEmpty(redirectUri)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.invalidRequest("Missing required parameters"))
                        .build();
            }

            // Check if user is authenticated
            if (securityContext.getUserPrincipal() == null) {
                // Redirect to login with return URL
                String returnUrl = uriInfo.getRequestUri().toString();
                return Response.seeOther(
                        UriBuilder.fromPath("/login")
                                .queryParam("return_url", returnUrl)
                                .build()
                ).build();
            }

            // User is authenticated, check consent
            User user = getCurrentUser();
            Set<String> requestedScopes = StringUtil.splitScopes(scope);

            // TODO: Check if consent is needed and show consent page
            // For now, auto-approve

            AuthorizationRequest request = AuthorizationRequest.builder()
                    .responseType(responseType)
                    .clientId(clientId)
                    .redirectUri(redirectUri)
                    .scope(scope)
                    .state(state)
                    .codeChallenge(codeChallenge)
                    .codeChallengeMethod(codeChallengeMethod)
                    .build();

            AuthorizationResponse response = authorizationService
                    .createAuthorizationCode(request, user, requestedScopes);

            // Redirect back to client with authorization code
            String location = HttpUtil.buildRedirectUri(redirectUri, Map.of(
                    "code", response.getCode(),
                    "state", state != null ? state : ""
            ));

            return Response.seeOther(UriBuilder.fromUri(location).build()).build();

        } catch (OAuth2Exception e) {
            // Redirect to client with error
            String location = HttpUtil.buildRedirectUri(redirectUri, Map.of(
                    "error", e.getError(),
                    "error_description", e.getErrorDescription(),
                    "state", state != null ? state : ""
            ));
            return Response.seeOther(UriBuilder.fromUri(location).build()).build();
        }
    }

    private User getCurrentUser() {
        // Get current authenticated user from security context
        // This is a placeholder - implement based on your security setup
        return new User();
    }
}