package org.marllon.caip.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.marllon.caip.dto.request.ReportRequest;
import org.marllon.caip.dto.response.ReportResponse;
import org.marllon.caip.dto.response.LocationResponse;
import org.marllon.caip.dto.response.PositionResponse;
import org.marllon.caip.dto.response.StatusStepDetailsResponse;
import org.marllon.caip.dto.response.summarys.UserResponseSummary;
import org.marllon.caip.model.Location;
import org.marllon.caip.model.Position;
import org.marllon.caip.model.Report;
import org.marllon.caip.model.StatusStep;
import org.marllon.caip.model.User;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReportMapper {


    /**
     *     |=========================================================|
     *     |    1. MAPEAMENTO DE SAÍDA (Entity -> Response)          |
     *     |=========================================================|
     * */

    @Mapping(source = "typeReport", target = "type")
    @Mapping(source = "position", target = "relationalPosition")
    @Mapping(source = "closed", target = "isFinish")
    @Mapping(source = "audit.createdBy", target = "createdBy")
    @Mapping(source = "audit.createdAt", target = "createdAt")
    ReportResponse toResponse(Report report);



    /**
     *     |====================================================================|
     *     |    2. MAPEAMENTO DE ENTRADA E ATUALIZAÇÃO (Request -> Entity)      |
     *     |====================================================================|
     *
     *   Tudo que por padrão o front não pode alterar nos apenas ignoramos pois
     *   Esses campos serão preenchidos pela lógica de negócio no Service (ou pelo JPA Auditing).
     * */

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "closed", ignore = true)
    @Mapping(target = "audit", ignore = true)
    @Mapping(target = "statusSteps", ignore = true)
    @Mapping(target = "foundBy", ignore = true)
    @Mapping(target = "collectedBy", ignore = true)
    Report toEntity(ReportRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "closed", ignore = true)
    @Mapping(target = "audit", ignore = true)
    @Mapping(target = "statusSteps", ignore = true)
    @Mapping(target = "foundBy", ignore = true)
    @Mapping(target = "collectedBy", ignore = true)
    void updateEntity(@MappingTarget Report report, ReportRequest request);

    /**
     *     |==================================================================|
     *     |     3. TRADUTORES AUXILIARES (MapStruct usa automaticamente)     |
     *     |==================================================================|
     * */
    UserResponseSummary toUserSummary(User user);
    LocationResponse toLocationResponse(Location location);
    PositionResponse toPositionResponse(Position position);
    StatusStepDetailsResponse toStatusStepDetailsResponse(StatusStep statusStep);
}