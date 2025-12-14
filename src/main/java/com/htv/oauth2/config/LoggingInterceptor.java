package com.htv.oauth2.config;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor for logging method calls
 */
@Slf4j
@Logged
//@Interceptor
@Dependent
class LoggingInterceptor {

    @AroundInvoke
    Object logMethodCall(InvocationContext context) throws Exception {
        String methodName = context.getMethod().getName();
        String className = context.getTarget().getClass().getSimpleName();

        log.debug("Entering {}.{}", className, methodName);
        long startTime = System.currentTimeMillis();

        try {
            Object result = context.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Exiting {}.{} ({}ms)", className, methodName, duration);
            return result;
        } catch (Exception e) {
            log.error("Exception in {}.{}: {}", className, methodName, e.getMessage());
            throw e;
        }
    }
}
