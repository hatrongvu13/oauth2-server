package com.htv.oauth2.repository;

import com.htv.oauth2.domain.AuditLog;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class AuditLogRepository implements PanacheRepositoryBase<AuditLog, String> {

    public List<AuditLog> findByUserId(String userId, Page page) {
        return find("user.id = ?1 order by createdAt desc", userId)
                .page(page)
                .list();
    }

    public List<AuditLog> findByAction(String action, Page page) {
        return find("action = ?1 order by createdAt desc", action)
                .page(page)
                .list();
    }

    public List<AuditLog> findByUserIdAndAction(String userId, String action, Page page) {
        return find("user.id = ?1 and action = ?2 order by createdAt desc", userId, action)
                .page(page)
                .list();
    }

    public List<AuditLog> findRecent(int limit) {
        return find("order by createdAt desc")
                .page(0, limit)
                .list();
    }

    public List<AuditLog> findFailedLogins(String userId, Instant since) {
        return list("user.id = ?1 and action = 'LOGIN' and status = 'FAILURE' and createdAt > ?2",
                userId, since);
    }

    public long countByAction(String action) {
        return count("action", action);
    }

    public long countByStatus(String status) {
        return count("status", status);
    }

    public long deleteOlderThan(Instant cutoff) {
        return delete("createdAt < ?1", cutoff);
    }

    public List<AuditLog> findByIpAddress(String ipAddress, Page page) {
        return find("ipAddress = ?1 order by createdAt desc", ipAddress)
                .page(page)
                .list();
    }
}
