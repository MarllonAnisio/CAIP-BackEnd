package org.marllon.caip.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthUserRequest(
        @NotBlank(message = "Registration não pode estar vazio")
        String registration,

        @NotBlank(message = "Registration não pode estar vazio")
        String password) {}
