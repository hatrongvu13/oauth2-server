package com.htv.oauth2.exception;

public class ClientNotFoundException extends OAuth2Exception {

    public ClientNotFoundException(String clientId) {
        super("client_not_found", "Client not found: " + clientId, 404);
    }
}
