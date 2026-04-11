package org.marllon.caip.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "The name cannot be empty.")
        String name,

        @NotBlank(message = "The Registration cannot be empty.")
        String registration,

        @NotBlank(message = "The Password cannot be empty.")
        @Size(min = 6, message = "The password must be at least 6 characters long.")
        String password
) {}
