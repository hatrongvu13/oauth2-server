package com.htv.oauth2.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SslOptions;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

@Slf4j
@ApplicationScoped
public class RedisProducerConfig {

    @ConfigProperty(name = "quarkus.redis.hosts")
    String redisUrl;

    private RedisClient redisClient;
    private StatefulRedisConnection<String, byte[]> connection;
    private ProxyManager<String> proxyManager;
    private ClientResources clientResources;

    void onStart(@Observes StartupEvent ev) {
        try {
            Thread.sleep(100); // Small delay for native image
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        initializeProxyManager();
    }

    @Produces
    @ApplicationScoped
    public ProxyManager<String> proxyManager() {
        if (proxyManager == null) {
            throw new IllegalStateException("ProxyManager not initialized");
        }
        return proxyManager;
    }

    private void initializeProxyManager() {
        try {
            log.info("🚀 Initializing Bucket4j ProxyManager...");

            // Create client resources (NO NATIVE TRANSPORT)
            clientResources = DefaultClientResources.builder()
                    .ioThreadPoolSize(4)
                    .computationThreadPoolSize(4)
                    .build();

            // Parse Redis URI
            RedisURI uri = RedisURI.create(redisUrl);
            uri.setTimeout(Duration.ofSeconds(5));

            // Create Redis client
            redisClient = RedisClient.create(clientResources, uri);

            // Configure client options - DISABLE NATIVE SSL
            redisClient.setOptions(ClientOptions.builder()
                    .autoReconnect(true)
                    .pingBeforeActivateConnection(true)
                    .suspendReconnectOnProtocolFailure(false)
                    // CRITICAL: Disable native SSL
                    .sslOptions(SslOptions.builder()
                            .jdkSslProvider()  // Use JDK SSL, not native
                            .build())
                    .build());

            // Create connection
            RedisCodec<String, byte[]> codec = RedisCodec.of(
                    StringCodec.UTF8,
                    ByteArrayCodec.INSTANCE
            );
            connection = redisClient.connect(codec);

            // Test connection
            String pong = connection.sync().ping();
            log.info("✅ Redis connected: {}", pong);

            // Create ProxyManager
            proxyManager = LettuceBasedProxyManager.builderFor(connection)
                    .withExpirationStrategy(
                            io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
                                    .basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(10))
                    )
                    .build();

            log.info("✅ Bucket4j ProxyManager ready");

        } catch (Exception e) {
            log.error("❌ Failed to initialize ProxyManager", e);
            throw new RuntimeException(e);
        }
    }

    void onShutdown(@Observes ShutdownEvent ev) {
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
        if (redisClient != null) {
            redisClient.shutdown();
        }
        if (clientResources != null) {
            clientResources.shutdown().notifyAll();
        }
        log.info("✅ Redis connections closed");
    }
}