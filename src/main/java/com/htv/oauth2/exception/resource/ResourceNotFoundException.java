package com.htv.oauth2.exception.resource;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class ResourceNotFoundException extends OAuth2Exception {

    public ResourceNotFoundException(String resourceType, String identifier) {
        super("resource_not_found", resourceType + " not found: " + identifier, 404);
    }
}
