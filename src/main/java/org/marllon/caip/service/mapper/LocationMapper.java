package org.marllon.caip.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.marllon.caip.dto.request.LocationRequest;
import org.marllon.caip.dto.response.LocationResponse;
import org.marllon.caip.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {

    LocationResponse toResponse(Location location);

    Location toEntity(LocationRequest request);
    void updateEntity(@MappingTarget Location entity, LocationRequest request);


}

// https://youtu.be/dw341XYIp4c?si=nmGAgumMHasOug7Q