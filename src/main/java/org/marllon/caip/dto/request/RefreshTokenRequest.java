package org.marllon.caip.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "The refreshToken cannot be empty.")
        String refreshToken
) {}
