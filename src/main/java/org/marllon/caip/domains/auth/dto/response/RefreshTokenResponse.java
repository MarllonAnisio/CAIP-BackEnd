package org.marllon.caip.domains.auth.dto.response;

public record RefreshTokenResponse(String accessToken,
                                   long accessExpiresIn,
                                   String refreshToken,
                                   long refreshExpiresIn) {
}
