package com.htv.oauth2.mapper;

import com.htv.oauth2.domain.AuditLog;
import com.htv.oauth2.dto.response.AuditLogResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors; /**
 * Manual implementation of AuditLogMapper
 */
@ApplicationScoped
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog auditLog) {
        if (auditLog == null) return null;

        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUser() != null ? auditLog.getUser().getId() : null)
                .username(auditLog.getUser() != null ? auditLog.getUser().getUsername() : null)
                .action(auditLog.getAction())
                .resource(auditLog.getResource())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .status(auditLog.getStatus())
                .errorMessage(auditLog.getErrorMessage())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    public List<AuditLogResponse> toResponseList(List<AuditLog> auditLogs) {
        if (auditLogs == null) return List.of();
        return auditLogs.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
