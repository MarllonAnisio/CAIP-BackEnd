package org.marllon.caip.dto.response;

import java.util.List;

public record UserResponse(
        Long id,
        String name,
        String registration,
        Boolean isActive,
        List<String> roles
) {}
