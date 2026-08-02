package org.marllon.caip.domains.auth.dto.response;

public record AuthUserResponse(
        String accessToken,
        long expiresInSeconds,
        String usernameOrEmail,
        String refreshToken,
        long refreshExpirationSeconds) {
}
