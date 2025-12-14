package com.htv.oauth2.exception;

public class ResourceNotFoundException extends OAuth2Exception {

    public ResourceNotFoundException(String resourceType, String identifier) {
        super("resource_not_found", resourceType + " not found: " + identifier, 404);
    }
}
