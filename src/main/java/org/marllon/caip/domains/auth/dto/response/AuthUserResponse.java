package org.marllon.caip.domains.auth.dto.response;

public record AuthUserResponse(
        String token,
        long expiresInSeconds,
        String usernameOrEmail,
        String refreshToken,
        long refreshExpirationSeconds) {
}
