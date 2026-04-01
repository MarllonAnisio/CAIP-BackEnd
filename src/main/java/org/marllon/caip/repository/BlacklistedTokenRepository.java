package org.marllon.caip.repository;

import org.marllon.caip.model.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    boolean existsByToken(String token);
    long deleteAllByExpiresAtBefore(Instant instant);
}
