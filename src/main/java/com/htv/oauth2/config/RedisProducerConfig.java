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

    private RedisClient redisClient;
    private StatefulRedisConnection<String, byte[]> connection;

    @Produces
    @ApplicationScoped
    public ProxyManager<String> proxyManager() {
        try {
            // Initialize Redis client
            redisClient = RedisClient.create(redisUrl);

            // Create connection with proper codec
            RedisCodec<String, byte[]> codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
            connection = redisClient.connect(codec);

            // Test connection
            String ping = connection.sync().ping();
            log.info("✅ Redis ProxyManager initialized successfully: {}", ping);

            // Create and return ProxyManager
            return LettuceBasedProxyManager.builderFor(connection).build();

        } catch (Exception e) {
            log.error("❌ Failed to initialize Redis ProxyManager", e);
            throw new RuntimeException("Redis ProxyManager initialization failed", e);
        }
    }

    @PreDestroy
    void cleanup() {
        if (connection != null) {
            connection.close();
            log.info("Redis connection closed");
        }
        if (redisClient != null) {
            redisClient.shutdown();
            log.info("Redis client shutdown");
        }
    }
}