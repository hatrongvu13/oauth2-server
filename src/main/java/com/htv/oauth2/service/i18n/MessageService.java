package com.htv.oauth2.service.i18n;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Service xử lý đa ngôn ngữ cho error messages
 */
@Slf4j
@ApplicationScoped
public class MessageService {

    private static final String BUNDLE_NAME = "messages";
    private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

    /**
     * Lấy message theo key và locale
     */
    public String getMessage(String key, Locale locale) {
        return getMessage(key, locale, null);
    }

    /**
     * Lấy message với parameters
     */
    public String getMessage(String key, Locale locale, Object... params) {
        try {
            Locale actualLocale = locale != null ? locale : DEFAULT_LOCALE;
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, actualLocale);
            String message = bundle.getString(key);

            if (params != null && params.length > 0) {
                return MessageFormat.format(message, params);
            }

            return message;
        } catch (Exception e) {
            log.warn("Message key not found: {} for locale: {}", key, locale);
            return key;
        }
    }

    /**
     * Lấy message với locale mặc định
     */
    public String getMessage(String key, Object... params) {
        return getMessage(key, DEFAULT_LOCALE, params);
    }

    /**
     * Parse locale từ string (vd: "en-US", "vi-VN")
     */
    public Locale parseLocale(String localeString) {
        if (localeString == null || localeString.isEmpty()) {
            return DEFAULT_LOCALE;
        }

        String[] parts = localeString.replace("-", "_").split("_");
        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else if (parts.length == 2) {
            return new Locale(parts[0], parts[1]);
        }

        return DEFAULT_LOCALE;
    }
}