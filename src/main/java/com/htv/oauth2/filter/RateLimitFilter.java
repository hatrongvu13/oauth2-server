package com.htv.oauth2.filter;

import com.htv.oauth2.service.RateLimitService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Provider
@Priority(1000)
public class RateLimitFilter implements ContainerRequestFilter {

    @Inject
    RateLimitService rateLimitService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // Apply rate limiting to login endpoint
        if (path.contains("/auth/login")) {
            String identifier = getIdentifier(requestContext);

            var result = rateLimitService.checkLogin(identifier);

            if (result.isBlocked()) {
                Response response = Response.status(429) // Too Many Requests
                        .header("X-RateLimit-Limit", 5)
                        .header("X-RateLimit-Remaining", 0)
                        .header("X-RateLimit-Reset", result.getRetryAfterSeconds())
                        .header("Retry-After", result.getRetryAfterSeconds())
                        .entity(new ErrorResponse(
                                "Too many login attempts",
                                "Please try again in " + result.getRetryAfterSeconds() + " seconds"
                        ))
                        .build();

                requestContext.abortWith(response);
                return;
            }

            // Add rate limit headers
            requestContext.getHeaders().add("X-RateLimit-Remaining",
                    String.valueOf(result.getRemainingTokens()));
        }
    }

    private String getIdentifier(ContainerRequestContext context) {
        // Get identifier from IP or username
        return context.getHeaderString("X-Forwarded-For") != null
                ? context.getHeaderString("X-Forwarded-For")
                : "unknown";
    }

    record ErrorResponse(String error, String message) {}
}