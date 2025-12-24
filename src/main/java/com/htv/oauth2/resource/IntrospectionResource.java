package com.htv.oauth2.resource;

import com.htv.oauth2.dto.response.ErrorResponse;
import com.htv.oauth2.dto.response.TokenIntrospectionResponse;
import com.htv.oauth2.service.client.ClientService;
import com.htv.oauth2.service.token.TokenService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/oauth2/introspect")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class IntrospectionResource {

    @Inject
    TokenService tokenService;

    @Inject
    ClientService clientService;

    /**
     * Token introspection endpoint
     * POST /oauth2/introspect
     */
    @POST
    @PermitAll
    public Response introspect(
            @FormParam("token") String token,
            @FormParam("token_type_hint") String tokenTypeHint,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret) {

        // Validate client
        clientService.validateClientCredentials(clientId, clientSecret);

        if (token == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ErrorResponse.invalidRequest("Missing token parameter"))
                    .build();
        }

        TokenIntrospectionResponse response = tokenService.introspectToken(token);
        return Response.ok(response).build();
    }
}
