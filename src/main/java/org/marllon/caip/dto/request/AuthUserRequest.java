package org.marllon.caip.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthUserRequest(
        @NotBlank(message = "The Registration cannot be empty.")
        String registration,

        @NotBlank(message = "The Password cannot be empty.")
        String password
) {}
