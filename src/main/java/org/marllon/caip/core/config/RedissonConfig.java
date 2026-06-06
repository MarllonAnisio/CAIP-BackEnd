package org.marllon.caip.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.jcache.JCachingProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StringUtils;

import javax.cache.CacheManager;
import javax.cache.Caching;
import java.net.URI;

@Profile("!test")
@Configuration
@Slf4j
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:#{null}}")
    private String password;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        // O Redisson usa por padrão o Jackson, mas é uma boa prática configurar explicitamente
        // para garantir que ele entenda os tipos de data/hora do Java 8+ (Instant, etc.)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        config.setCodec(new JsonJacksonCodec(objectMapper));

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

        log.info("Conectando ao Redis em {}:{}", host, port);
        return Redisson.create(config);
    }

    @Bean
    public CacheManager jCacheManager(RedissonClient redissonClient) {
        URI uri = URI.create("classpath:redisson-jcache.yaml");
        return Caching.getCachingProvider(JCachingProvider.class.getName())
                .getCacheManager(uri, null, redissonClient);
    }
}
