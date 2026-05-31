package org.marllon.caip.domains.location.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.marllon.caip.domains.location.dto.request.LocationRequest;
import org.marllon.caip.domains.location.dto.response.LocationResponse;
import org.marllon.caip.domains.location.entity.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationResponse toResponse(Location location);

    Location toEntity(LocationRequest request);
    void updateEntity(@MappingTarget Location entity, LocationRequest request);


}

