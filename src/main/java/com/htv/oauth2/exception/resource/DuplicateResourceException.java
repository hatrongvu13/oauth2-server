package com.htv.oauth2.exception.resource;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class DuplicateResourceException extends OAuth2Exception {

    public DuplicateResourceException(String message) {
        super("duplicate_resource", message, 409);
    }
}
