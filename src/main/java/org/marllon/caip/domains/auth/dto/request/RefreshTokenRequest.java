package org.marllon.caip.domains.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "The refreshToken cannot be empty.")
        String refreshToken
) {}
