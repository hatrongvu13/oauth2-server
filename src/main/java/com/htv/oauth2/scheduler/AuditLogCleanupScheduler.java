package com.htv.oauth2.scheduler;

import com.htv.oauth2.repository.AuditLogRepository;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@ApplicationScoped
public class AuditLogCleanupScheduler {

    @Inject
    AuditLogRepository auditLogRepository;

    /**
     * Clean up old audit logs every day at 2 AM
     * Keep logs for 90 days
     */
    @Scheduled(cron = "0 0 2 * * ?", identity = "audit-cleanup")
    void cleanupOldAuditLogs() {
        log.info("Starting audit log cleanup job");
        try {
            Instant cutoff = Instant.now().minus(90, ChronoUnit.DAYS);
            long deleted = auditLogRepository.deleteOlderThan(cutoff);
            log.info("Audit log cleanup completed, deleted {} records", deleted);
        } catch (Exception e) {
            log.error("Audit log cleanup failed", e);
        }
    }
}
