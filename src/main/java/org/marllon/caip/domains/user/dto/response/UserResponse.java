package org.marllon.caip.domains.user.dto.response;

public record UserResponse(
        Long id,
        String name,
        String registration,
        Boolean isActive,
        String role
) {}
