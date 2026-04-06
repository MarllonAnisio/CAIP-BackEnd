package org.marllon.caip.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record UpdateUserRolesRequest(
        @NotBlank(message = "The Roles cannot be empty.")
        List<String> roles
) {}
