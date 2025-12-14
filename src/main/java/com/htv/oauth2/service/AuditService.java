package com.htv.oauth2.service;

import com.htv.oauth2.domain.AuditLog;
import com.htv.oauth2.domain.User;
import com.htv.oauth2.repository.AuditLogRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class AuditService {

    @Inject
    AuditLogRepository auditLogRepository;

    /**
     * Log successful action
     */
    @Transactional
    public void logSuccess(User user, String action, String resource, String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .resource(resource)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .status("SUCCESS")
                    .build();

            auditLogRepository.persist(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Log failed action
     */
    @Transactional
    public void logFailure(User user, String action, String resource, String errorMessage,
                           String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .resource(resource)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .status("FAILURE")
                    .errorMessage(errorMessage)
                    .build();

            auditLogRepository.persist(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Log action without user (e.g., failed login attempts)
     */
    @Transactional
    public void logAnonymous(String action, String resource, String status,
                             String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .resource(resource)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .status(status)
                    .build();

            auditLogRepository.persist(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }
}