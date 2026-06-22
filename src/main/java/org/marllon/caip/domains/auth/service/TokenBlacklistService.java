package org.marllon.caip.domains.auth.service;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.time.Instant;


@Service
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "blacklist:";
    private final RedissonClient redissonClient;
    public TokenBlacklistService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
    public void blacklist(String token, Instant expiresAt) {
        if (token == null || token.isBlank()) return;

        /**
         * Calcula o tempo de vida (TTL) restante do token
         * */
        long ttl = Duration.between(Instant.now(), expiresAt != null ? expiresAt : Instant.now()).toMillis();

        /**
         * Se já estiver expirado, não precisamos nem salvar no Redis(o token já não é mais válido)
         * */
        if (ttl <= 0) return;

        /**
         * Salva a chave no Redis e já atribui o TTL automático
         * */
        RBucket<Boolean> bucket = redissonClient.getBucket(KEY_PREFIX + token);
        bucket.set(Boolean.TRUE, Duration.ofMillis(ttl));
    }
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) return false;

        /**
         * Checa rapidamente no Redis se o token está revogado
         * */
        return redissonClient.getBucket(KEY_PREFIX + token).isExists();
    }
}
