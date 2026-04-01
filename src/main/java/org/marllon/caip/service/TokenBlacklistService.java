package org.marllon.caip.service;

import org.marllon.caip.model.BlacklistedToken;
import org.marllon.caip.repository.BlacklistedTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;


@Service
public class TokenBlacklistService {

    private final BlacklistedTokenRepository repository;

    public TokenBlacklistService(BlacklistedTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void blacklist(String token, Instant expiresAt) {
        if (token == null || token.isBlank()) return;
        if (repository.existsByToken(token)) return;
        repository.save(BlacklistedToken.builder()
                .token(token)
                .expiresAt(expiresAt != null ? expiresAt : Instant.now())
                .build());
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) return false;
        return repository.existsByToken(token);
    }

    @Scheduled(fixedDelay = 6 * 60 * 60 * 1000L, initialDelay = 60 * 1000L)
    @Transactional
    public void purgeExpired() {
        repository.deleteAllByExpiresAtBefore(Instant.now());
    }
}
