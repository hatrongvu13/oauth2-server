package com.htv.oauth2.resource;

import com.htv.oauth2.dto.response.ErrorResponse;
import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;
import com.htv.oauth2.service.client.ClientService;
import com.htv.oauth2.service.token.TokenService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/oauth2/revoke")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class RevocationResource {

    @Inject
    TokenService tokenService;

    @Inject
    ClientService clientService;

    /**
     * Token revocation endpoint
     * POST /oauth2/revoke
     */
    @POST
    @PermitAll
    public Response revoke(
            @FormParam("token") String token,
            @FormParam("token_type_hint") String tokenTypeHint,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret) {

        try {
            // Validate client
            clientService.validateClientCredentials(clientId, clientSecret);

            if (token == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ErrorResponse.invalidRequest("Missing token parameter"))
                        .build();
            }

            tokenService.revokeToken(token);
            return Response.ok().build();

        } catch (OAuth2Exception e) {
            ErrorResponse error = ErrorResponse.builder()
                    .error(e.getError())
                    .errorDescription(e.getErrorDescription())
                    .build();
            return Response.status(e.getHttpStatus()).entity(error).build();
        }
    }
}
