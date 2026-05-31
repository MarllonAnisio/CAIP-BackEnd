package org.marllon.caip.domains.location.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LocationRequest(
        @NotBlank(message = "The location name cannot be empty.")
        String name) {}
