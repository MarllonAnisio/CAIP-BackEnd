package org.marllon.caip.domains.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateUserRolesRequest(
        @NotNull(message = "role não pode ser null")
        List<String> roles
) {}
