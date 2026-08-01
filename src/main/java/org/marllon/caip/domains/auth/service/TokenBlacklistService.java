package org.marllon.caip.domains.auth.service;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.time.Instant;


@Service
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "blacklist:";
    private static final Duration MIN_TTL = Duration.ofSeconds(60);
    private final RedissonClient redissonClient;
    public TokenBlacklistService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
    public void blacklist(String token, Instant expiresAt) {
        if (token == null || token.isBlank()) return;

        /**
         * Calcula o tempo de vida (TTL) restante do token
         * */
        Instant now = Instant.now();
        Instant safeExpiresAt = expiresAt != null ? expiresAt : now;

        // Calcula o tempo de vida (TTL) restante do token
        Duration ttl = Duration.between(now, safeExpiresAt);

        // Se já estiver expirado ou muito próximo do vencimento,
        // aplicamos um TTL mínimo para evitar race conditions e clock skew entre servidores.
        if (ttl.isNegative() || ttl.isZero() || ttl.compareTo(MIN_TTL) < 0) {
            ttl = MIN_TTL;
        }

        // Salva a chave no Redis e já atribui o TTL automático
        RBucket<Boolean> bucket = redissonClient.getBucket(KEY_PREFIX + token);
        bucket.set(Boolean.TRUE, ttl);
    }
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) return false;

        /**
         * Checa rapidamente no Redis se o token está revogado
         * */
        return redissonClient.getBucket(KEY_PREFIX + token).isExists();
    }
}
