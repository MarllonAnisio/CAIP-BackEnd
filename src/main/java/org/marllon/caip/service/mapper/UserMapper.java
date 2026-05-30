package org.marllon.caip.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.marllon.caip.dto.request.UserRequest;
import org.marllon.caip.dto.response.UserResponse;
import org.marllon.caip.model.entity.User;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserResponse toResponse(User user);

    User toEntity(UserRequest request);

    @Mapping(target = "id", ignore = true)        // Nunca muda o ID via DTO
    @Mapping(target = "role", ignore = true)      // Ignora a Role (tratado no Service)
    @Mapping(target = "password", ignore = true)  // Senha tem lógica customizada de BCrypt
    void updateEntity(@MappingTarget User user, UserRequest dto);
}