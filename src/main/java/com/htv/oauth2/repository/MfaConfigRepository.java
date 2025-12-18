package com.htv.oauth2.repository;

import com.htv.oauth2.domain.MfaConfig;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MfaConfigRepository implements PanacheRepositoryBase<MfaConfig, String> {
    public MfaConfig findByUserId(String userId) {
        return find("userId", userId).firstResult();
    }
}
