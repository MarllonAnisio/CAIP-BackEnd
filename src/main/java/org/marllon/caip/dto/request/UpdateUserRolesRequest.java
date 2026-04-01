package org.marllon.caip.dto.request;

import java.util.List;

public record UpdateUserRolesRequest(List<String> roles) {
}
