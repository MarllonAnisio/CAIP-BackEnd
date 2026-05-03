package org.marllon.caip.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.util.List;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${REDIS_PASSWORD:#{null}}")
    private String password;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.setCodec(new JsonJacksonCodec());

        var server = config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setIdleConnectionTimeout(10_000)
                .setConnectTimeout(10_000)
                .setTimeout(3_000)
                .setRetryAttempts(3)
                .setRetryInterval(1_500);

        if (StringUtils.hasText(password)) {
            server.setPassword(password);
        }

        return Redisson.create(config);
    }

    /**
     * JCache CacheManager backed by Redisson.
     *
     * Serves two consumers:
     *   1. Spring Cache abstraction (@Cacheable, @CacheEvict) — Spring Boot wraps this
     *      in JCacheCacheManager automatically when spring.cache.type=jcache.
     *   2. Bucket4j rate limiting — directly uses javax.cache.CacheManager
     *      when cache-to-use: jcache is configured.
     *
     * All required caches are pre-created here so both consumers find them
     * immediately at startup, without relying on lazy creation.
     */
    @Bean
    public CacheManager jCacheManager(RedissonClient redissonClient) {
        MutableConfiguration<Object, Object> jcacheConfig = new MutableConfiguration<>()
                .setStoreByValue(false)
                .setStatisticsEnabled(false);

        var redissonCacheConfig = RedissonConfiguration.fromInstance(redissonClient, jcacheConfig);

        CachingProvider provider = Caching.getCachingProvider("org.redisson.jcache.JCachingProvider");
        CacheManager manager = provider.getCacheManager();

        for (String cacheName : List.of("rate-limit-login", "locations", "tb_report")) {
            if (manager.getCache(cacheName) == null) {
                manager.createCache(cacheName, redissonCacheConfig);
            }
        }

        return manager;
    }
}
