package com.htv.oauth2.mapper;

import com.htv.oauth2.domain.Client;
import com.htv.oauth2.dto.request.ClientRegistrationRequest;
import com.htv.oauth2.dto.request.ClientUpdateRequest;
import com.htv.oauth2.dto.response.ClientListResponse;
import com.htv.oauth2.dto.response.ClientResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors; /**
 * Manual implementation of ClientMapper
 */
@ApplicationScoped
public class ClientMapper {

    public ClientResponse toResponse(Client client) {
        if (client == null) return null;

        return ClientResponse.builder()
                .clientId(client.getClientId())
                .clientName(client.getClientName())
                .description(client.getDescription())
                .redirectUris(client.getRedirectUris())
                .grantTypes(client.getGrantTypes())
                .scopes(client.getScopes())
                .accessTokenValidity(client.getAccessTokenValidity())
                .refreshTokenValidity(client.getRefreshTokenValidity())
                .autoApprove(client.getAutoApprove())
                .enabled(client.getEnabled())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }

    public ClientResponse toResponseWithSecret(Client client) {
        if (client == null) return null;

        ClientResponse response = toResponse(client);
        response.setClientSecret(client.getClientSecret());
        return response;
    }

    public List<ClientResponse> toResponseList(List<Client> clients) {
        if (clients == null) return List.of();
        return clients.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ClientListResponse.ClientSummary toSummary(Client client) {
        if (client == null) return null;

        return ClientListResponse.ClientSummary.builder()
                .clientId(client.getClientId())
                .clientName(client.getClientName())
                .description(client.getDescription())
                .enabled(client.getEnabled())
                .createdAt(client.getCreatedAt())
                .build();
    }

    public List<ClientListResponse.ClientSummary> toSummaryList(List<Client> clients) {
        if (clients == null) return List.of();
        return clients.stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public Client fromRegistrationRequest(ClientRegistrationRequest request) {
        if (request == null) return null;

        return Client.builder()
                .clientName(request.getClientName())
                .description(request.getDescription())
                .redirectUris(request.getRedirectUris())
                .grantTypes(request.getGrantTypes())
                .scopes(request.getScopes())
                .accessTokenValidity(request.getAccessTokenValidity() != null ?
                        request.getAccessTokenValidity() : 3600)
                .refreshTokenValidity(request.getRefreshTokenValidity() != null ?
                        request.getRefreshTokenValidity() : 86400)
                .autoApprove(request.getAutoApprove() != null ?
                        request.getAutoApprove() : false)
                .enabled(true)
                .build();
    }

    public void updateClientFromRequest(ClientUpdateRequest request, Client client) {
        if (request == null || client == null) return;

        if (request.getClientName() != null) {
            client.setClientName(request.getClientName());
        }
        if (request.getDescription() != null) {
            client.setDescription(request.getDescription());
        }
        if (request.getRedirectUris() != null) {
            client.setRedirectUris(request.getRedirectUris());
        }
        if (request.getGrantTypes() != null) {
            client.setGrantTypes(request.getGrantTypes());
        }
        if (request.getScopes() != null) {
            client.setScopes(request.getScopes());
        }
        if (request.getAccessTokenValidity() != null) {
            client.setAccessTokenValidity(request.getAccessTokenValidity());
        }
        if (request.getRefreshTokenValidity() != null) {
            client.setRefreshTokenValidity(request.getRefreshTokenValidity());
        }
        if (request.getAutoApprove() != null) {
            client.setAutoApprove(request.getAutoApprove());
        }
        if (request.getEnabled() != null) {
            client.setEnabled(request.getEnabled());
        }
    }
}
