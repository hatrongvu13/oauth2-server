package com.htv.oauth2.exception.core;

import com.htv.oauth2.exception.auth.oauth2.OAuth2Exception;

public class CacheException extends OAuth2Exception {

    public CacheException(String message, Throwable cause) {
        super("cache_error", message, 500, cause);
    }

    public CacheException(String message) {
        super("cache_error", message, 500);
    }
}
