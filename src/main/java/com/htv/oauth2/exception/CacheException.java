package com.htv.oauth2.exception;

public class CacheException extends OAuth2Exception {

    public CacheException(String message, Throwable cause) {
        super("cache_error", message, 500, cause);
    }
}
