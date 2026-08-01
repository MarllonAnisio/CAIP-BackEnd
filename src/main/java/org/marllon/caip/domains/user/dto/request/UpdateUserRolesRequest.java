package org.marllon.caip.domains.user.dto.request;

import jakarta.validation.constraints.NotNull;
import org.marllon.caip.domains.user.entity.constants.Role;

public record UpdateUserRolesRequest(
        @NotNull(message = "The role cannot be null.")
        Role role
) {}
