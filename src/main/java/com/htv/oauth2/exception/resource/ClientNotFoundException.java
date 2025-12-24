package com.htv.oauth2.exception.resource;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class ClientNotFoundException extends OAuth2Exception {

    public ClientNotFoundException(String clientId) {
        super("client_not_found", "Client not found: " + clientId, 404);
    }
}
