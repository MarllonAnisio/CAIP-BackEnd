package org.marllon.caip.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.marllon.caip.dto.request.UserRequest;
import org.marllon.caip.dto.response.UserResponse;
import org.marllon.caip.model.Role;
import org.marllon.caip.model.User;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface UserMapper {

    @Mapping(source = "roles", target = "roles")
    UserResponse toResponse(User user);


    User toEntity(UserRequest request);

    default String map(Role role) {
        if (role == null) {
            return null;
        }
        return role.getName();
    }

    @Mapping(target = "id", ignore = true)      // Nunca muda o ID via DTO
    @Mapping(target = "roles", ignore = true)   // Já tratamos as roles no Service
    @Mapping(target = "password", ignore = true)// Senha tem lógica customizada de BCrypt
    void updateEntity(@MappingTarget User user, UserRequest dto);
}
