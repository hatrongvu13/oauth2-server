package com.htv.oauth2.exception;

public class ServiceUnavailableException extends OAuth2Exception {

    public ServiceUnavailableException(String message) {
        super("service_unavailable", message, 503);
    }
}
