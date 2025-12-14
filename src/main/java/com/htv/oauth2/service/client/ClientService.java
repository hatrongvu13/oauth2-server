package com.htv.oauth2.service.client;

import com.htv.oauth2.domain.Client;
import com.htv.oauth2.dto.request.ClientRegistrationRequest;
import com.htv.oauth2.dto.request.ClientUpdateRequest;
import com.htv.oauth2.dto.response.ClientResponse;
import com.htv.oauth2.exception.*;
import com.htv.oauth2.mapper.ClientMapper;
import com.htv.oauth2.repository.ClientRepository;
import com.htv.oauth2.util.CryptoUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@ApplicationScoped
public class ClientService {

    @Inject
    ClientRepository clientRepository;

    @Inject
    ClientMapper clientMapper;

    /**
     * Register new OAuth2 client
     */
    @Transactional
    public ClientResponse registerClient(ClientRegistrationRequest request) {
        log.info("Registering new OAuth2 client: {}", request.getClientName());

        // Create client entity
        Client client = clientMapper.fromRegistrationRequest(request);

        // Generate client credentials
        client.setClientId(CryptoUtil.generateClientId());
        String clientSecret = CryptoUtil.generateClientSecret();
        client.setClientSecret(clientSecret); // In production, hash this

        clientRepository.persist(client);

        log.info("Client registered successfully: {}", client.getClientId());

        // Return response with secret (only shown once)
        ClientResponse response = clientMapper.toResponseWithSecret(client);
        response.setClientSecret(clientSecret);
        return response;
    }

    /**
     * Find client by client ID
     */
    public ClientResponse findByClientId(String clientId) {
        Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
        return clientMapper.toResponse(client);
    }

    /**
     * Update client
     */
    @Transactional
    public ClientResponse updateClient(String clientId, ClientUpdateRequest request) {
        log.info("Updating client: {}", clientId);

        Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        clientMapper.updateClientFromRequest(request, client);
        clientRepository.persist(client);

        log.info("Client updated successfully: {}", clientId);
        return clientMapper.toResponse(client);
    }

    /**
     * Reset client secret
     */
    @Transactional
    public ClientResponse resetClientSecret(String clientId) {
        log.info("Resetting client secret: {}", clientId);

        Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        String newSecret = CryptoUtil.generateClientSecret();
        client.setClientSecret(newSecret); // In production, hash this
        clientRepository.persist(client);

        log.info("Client secret reset: {}", clientId);

        ClientResponse response = clientMapper.toResponseWithSecret(client);
        response.setClientSecret(newSecret);
        return response;
    }

    /**
     * Enable/disable client
     */
    @Transactional
    public void setClientEnabled(String clientId, boolean enabled) {
        Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
        client.setEnabled(enabled);
        clientRepository.persist(client);
        log.info("Client {} set to enabled={}", clientId, enabled);
    }

    /**
     * Delete client
     */
    @Transactional
    public void deleteClient(String clientId) {
        Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
        clientRepository.delete(client);
        log.info("Client deleted: {}", clientId);
    }

    /**
     * List all clients
     */
    public List<ClientResponse> listAllClients() {
        List<Client> clients = clientRepository.listAll();
        return clientMapper.toResponseList(clients);
    }

    /**
     * Validate client credentials
     */
    public Client validateClientCredentials(String clientId, String clientSecret) {
        Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new InvalidClientException("Invalid client credentials"));

        if (!client.getEnabled()) {
            throw new InvalidClientException("Client is disabled");
        }

        // In production, use password hashing for client secrets
        if (!client.getClientSecret().equals(clientSecret)) {
            throw new InvalidClientException("Invalid client credentials");
        }

        return client;
    }

    /**
     * Validate redirect URI
     */
    public void validateRedirectUri(Client client, String redirectUri) {
        if (!client.isValidRedirectUri(redirectUri)) {
            throw new InvalidRedirectUriException(
                    "Redirect URI not registered for this client: " + redirectUri
            );
        }
    }

    /**
     * Validate grant type
     */
    public void validateGrantType(Client client, String grantType) {
        if (!client.supportsGrantType(grantType)) {
            throw new UnsupportedGrantTypeException(grantType);
        }
    }
}