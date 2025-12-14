package com.htv.oauth2.exception;

public class DuplicateResourceException extends OAuth2Exception {

    public DuplicateResourceException(String message) {
        super("duplicate_resource", message, 409);
    }
}
