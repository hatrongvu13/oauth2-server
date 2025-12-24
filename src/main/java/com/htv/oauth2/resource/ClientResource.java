package com.htv.oauth2.resource;

import com.htv.oauth2.dto.request.client.ClientRegistrationRequest;
import com.htv.oauth2.dto.response.ClientResponse;
import com.htv.oauth2.service.client.ClientService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Path("/api/clients")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClientResource {

    @Inject
    ClientService clientService;

    /**
     * Register new client
     * POST /api/clients
     */
    @POST
    public Response register(@Valid ClientRegistrationRequest request) {
        ClientResponse client = clientService.registerClient(request);
        return Response.status(Response.Status.CREATED)
                .entity(client)
                .build();
    }

    /**
     * Get client by ID
     * GET /api/clients/{clientId}
     */
    @GET
    @Path("/{clientId}")
    public Response getClient(@PathParam("clientId") String clientId) {
        ClientResponse client = clientService.findByClientId(clientId);
        return Response.ok(client).build();
    }

    /**
     * List all clients
     * GET /api/clients
     */
    @GET
    public Response listClients() {
        return Response.ok(clientService.listAllClients()).build();
    }
}
