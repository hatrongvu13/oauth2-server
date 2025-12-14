package com.htv.oauth2.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;


@Slf4j
@ApplicationScoped
public class ApplicationConfig {

    @ConfigProperty(name = "quarkus.application.name")
    String applicationName;

    @ConfigProperty(name = "quarkus.application.version")
    String applicationVersion;

    void onStart(@Observes StartupEvent event) {
        log.info("========================================");
        log.info("Starting {} v{}", applicationName, applicationVersion);
        log.info("========================================");
    }
}


