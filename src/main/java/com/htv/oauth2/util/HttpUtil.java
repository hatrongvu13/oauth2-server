package com.htv.oauth2.util;

import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@ApplicationScoped
public class HttpUtil {

    /**
     * Extract client IP from request
     */
    public static String getClientIp(jakarta.ws.rs.core.HttpHeaders headers) {
        // Check X-Forwarded-For header first (proxy/load balancer)
        String xff = headers.getHeaderString("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }

        // Check X-Real-IP header
        String xrip = headers.getHeaderString("X-Real-IP");
        if (xrip != null && !xrip.isEmpty()) {
            return xrip;
        }

        return "unknown";
    }

    /**
     * Extract user agent from request
     */
    public static String getUserAgent(jakarta.ws.rs.core.HttpHeaders headers) {
        String ua = headers.getHeaderString("User-Agent");
        return ua != null ? ua : "unknown";
    }

    /**
     * Build redirect URI with query parameters
     */
    public static String buildRedirectUri(String baseUri, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return baseUri;
        }

        StringBuilder sb = new StringBuilder(baseUri);
        boolean hasQuery = baseUri.contains("?");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(hasQuery ? "&" : "?");
            sb.append(entry.getKey()).append("=").append(urlEncode(entry.getValue()));
            hasQuery = true;
        }

        return sb.toString();
    }

    /**
     * URL encode a string
     */
    public static String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
