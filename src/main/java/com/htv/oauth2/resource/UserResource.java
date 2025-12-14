package com.htv.oauth2.resource;

import com.htv.oauth2.dto.request.RegisterRequest;
import com.htv.oauth2.dto.request.UserUpdateRequest;
import com.htv.oauth2.dto.response.ErrorResponse;
import com.htv.oauth2.dto.response.UserResponse;
import com.htv.oauth2.exception.OAuth2Exception;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    com.htv.oauth2.service.user.UserService userService;

    /**
     * Register new user
     * POST /api/users/register
     */
    @POST
    @Path("/register")
    @PermitAll
    public Response register(@Valid RegisterRequest request) {
        try {
            UserResponse user = userService.registerUser(request);
            return Response.status(Response.Status.CREATED)
                    .entity(user)
                    .build();
        } catch (OAuth2Exception e) {
            ErrorResponse error = ErrorResponse.builder()
                    .error(e.getError())
                    .errorDescription(e.getErrorDescription())
                    .build();
            return Response.status(e.getHttpStatus()).entity(error).build();
        }
    }

    /**
     * Get current user profile
     * GET /api/users/me
     */
    @GET
    @Path("/me")
    public Response getCurrentUser(@Context SecurityContext securityContext) {
        String userId = securityContext.getUserPrincipal().getName();
        UserResponse user = userService.findById(userId);
        return Response.ok(user).build();
    }

    /**
     * Update user profile
     * PUT /api/users/me
     */
    @PUT
    @Path("/me")
    public Response updateProfile(
            @Valid UserUpdateRequest request,
            @Context SecurityContext securityContext) {
        String userId = securityContext.getUserPrincipal().getName();
        UserResponse user = userService.updateUser(userId, request);
        return Response.ok(user).build();
    }
}
