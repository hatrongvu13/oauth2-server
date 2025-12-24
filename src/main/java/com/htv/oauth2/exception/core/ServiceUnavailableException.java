package com.htv.oauth2.exception.core;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class ServiceUnavailableException extends OAuth2Exception {

    public ServiceUnavailableException(String message) {
        super("service_unavailable", message, 503);
    }
}
