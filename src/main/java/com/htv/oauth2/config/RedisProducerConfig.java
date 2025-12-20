package com.htv.oauth2.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
@ApplicationScoped
public class RedisProducerConfig {

    @ConfigProperty(name = "quarkus.redis.hosts", defaultValue = "redis://localhost:6379")
    String redisUrl;

    private volatile RedisClient redisClient;
    private volatile StatefulRedisConnection<String, byte[]> connection;
    private volatile ProxyManager<String> proxyManager;

    @Produces
    @ApplicationScoped
    public ProxyManager<String> proxyManager() {
        // Lazy initialization để tránh lỗi GraalVM native image
        if (proxyManager == null) {
            synchronized (this) {
                if (proxyManager == null) {
                    initializeRedisConnection();
                }
            }
        }
        return proxyManager;
    }

    private void initializeRedisConnection() {
        try {
            log.info("Initializing Redis connection for Bucket4j...");

            // Create Redis client lazily
            redisClient = RedisClient.create(redisUrl);

            // Create connection with proper codec
            RedisCodec<String, byte[]> codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
            connection = redisClient.connect(codec);

            // Test connection
            String ping = connection.sync().ping();
            log.info("✅ Redis ProxyManager initialized successfully: {}", ping);

            // Create ProxyManager
            proxyManager = LettuceBasedProxyManager.builderFor(connection).build();

        } catch (Exception e) {
            log.error("❌ Failed to initialize Redis ProxyManager", e);
            throw new RuntimeException("Redis ProxyManager initialization failed", e);
        }
    }

    @PreDestroy
    void cleanup() {
        log.info("Shutting down Redis connections...");

        if (connection != null && connection.isOpen()) {
            try {
                connection.close();
                log.info("Redis connection closed");
            } catch (Exception e) {
                log.error("Error closing Redis connection", e);
            }
        }

        if (redisClient != null) {
            try {
                redisClient.shutdown();
                log.info("Redis client shutdown");
            } catch (Exception e) {
                log.error("Error shutting down Redis client", e);
            }
        }
    }
}