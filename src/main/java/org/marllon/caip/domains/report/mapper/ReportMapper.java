package org.marllon.caip.domains.report.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.marllon.caip.domains.report.dto.request.ReportRequest;
import org.marllon.caip.domains.report.dto.response.ReportResponse;
import org.marllon.caip.domains.location.dto.response.LocationResponse;
import org.marllon.caip.domains.location.dto.response.PositionResponse;
import org.marllon.caip.domains.report.dto.response.StatusStepDetailsResponse;
import org.marllon.caip.domains.user.dto.response.summary.UserResponseSummary;
import org.marllon.caip.domains.location.entity.Location;
import org.marllon.caip.domains.location.entity.Position;
import org.marllon.caip.domains.report.entity.Report;
import org.marllon.caip.domains.report.entity.StatusStep;
import org.marllon.caip.domains.user.entity.User;

import java.time.Instant;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, imports = Instant.class)
public interface ReportMapper {

    // ====================================================================
    // 1. MAPEAMENTO DE SAÍDA (Entity -> Response)
    // ====================================================================

    @Mapping(source = "typeReport", target = "type")
    @Mapping(source = "position", target = "relationalPosition")
    @Mapping(source = "closed", target = "isFinish")
    @Mapping(source = "audit.createdBy", target = "createdBy")
    @Mapping(source = "audit.createdAt", target = "createdAt")
    ReportResponse toResponse(Report report);

    // ====================================================================
    // 2. MAPEAMENTO DE ENTRADA E ATUALIZAÇÃO (Request -> Entity)
    // ====================================================================

    /**
     * Converte o DTO de requisição e os dados de contexto (autor, localização)
     * em uma entidade Report pronta para ser salva.
     * O MapStruct implementa este método automaticamente.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isClosed", constant = "false")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "statusSteps", ignore = true)
    @Mapping(target = "foundBy", ignore = true)
    @Mapping(target = "collectedBy", ignore = true)
    @Mapping(target = "dateReclamed", ignore = true)
    @Mapping(target = "date", expression = "java(Instant.now())")
    @Mapping(source = "request.title", target = "title")
    @Mapping(source = "request.description", target = "description")
    @Mapping(source = "request.imageUrl", target = "imageUrl")
    @Mapping(source = "request.typeReport", target = "typeReport")
    @Mapping(source = "location", target = "location")
    @Mapping(source = "request.position", target = "position")
    Report toEntity(ReportRequest request, User author, Location location);

    /**
     * Atualiza uma entidade Report existente com os dados de um DTO.
     * Ignora campos que não devem ser alterados em uma atualização.
     */
    @BeanMapping(builder = @org.mapstruct.Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "closed", ignore = true)
    @Mapping(target = "audit", ignore = true)
    @Mapping(target = "statusSteps", ignore = true)
    @Mapping(target = "foundBy", ignore = true)
    @Mapping(target = "collectedBy", ignore = true)
    @Mapping(target = "date", ignore = true)
    void updateEntity(@MappingTarget Report report, ReportRequest request);

    // ====================================================================
    // 3. TRADUTORES AUXILIARES (MapStruct usa automaticamente)
    // ====================================================================
    UserResponseSummary toUserSummary(User user);
    LocationResponse toLocationResponse(Location location);
    PositionResponse toPositionResponse(Position position);
    StatusStepDetailsResponse toStatusStepDetailsResponse(StatusStep statusStep);
}
