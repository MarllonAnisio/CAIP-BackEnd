package org.marllon.caip.dto.response;

public record RefreshTokenResponse(String accessToken,
                                   long accessExpiresIn,
                                   String refreshToken,
                                   long refreshExpiresIn) {
}
