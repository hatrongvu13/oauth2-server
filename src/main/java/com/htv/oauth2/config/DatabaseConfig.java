package com.htv.oauth2.config;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

@Getter
@ApplicationScoped
public class DatabaseConfig {

    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    Optional<String> jdbcUrl;

    @ConfigProperty(name = "quarkus.datasource.username")
    Optional<String> username;

    @ConfigProperty(name = "quarkus.hibernate-orm.log.sql", defaultValue = "false")
    Boolean logSql;
}
