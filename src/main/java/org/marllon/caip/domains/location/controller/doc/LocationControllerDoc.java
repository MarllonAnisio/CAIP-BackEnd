package org.marllon.caip.domains.location.controller.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.marllon.caip.core.exceptions.error.StandardError;
import org.marllon.caip.domains.location.dto.request.LocationRequest;
import org.marllon.caip.domains.location.dto.response.LocationResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Localizações", description = "Gerenciamento de localizações físicas do campus (prédios, salas, blocos)")
public interface LocationControllerDoc {

    @Operation(summary = "Lista todas as localizações",
            description = "Retorna todas as localizações cadastradas no sistema. "
                    + "Este endpoint utiliza cache Redis para otimizar consultas frequentes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de localizações retornada com sucesso")
    })
    ResponseEntity<List<LocationResponse>> findAll();

    @Operation(summary = "Busca uma localização por ID",
            description = "Retorna as informações de uma localização específica pelo seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Localização encontrada"),
            @ApiResponse(responseCode = "404", description = "Localização não encontrada",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<LocationResponse> findById(Long id);

    @Operation(summary = "Cadastra uma nova localização",
            description = "Cria uma nova localização no sistema. Não permite nomes duplicados (case-insensitive). "
                    + "Acesso restrito a ADMIN e LIBRARIAN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Localização criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou localização já cadastrada",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado — permissão insuficiente",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<LocationResponse> insert(LocationRequest request);

    @Operation(summary = "Atualiza uma localização existente",
            description = "Atualiza o nome de uma localização pelo seu ID. Valida duplicidade de nomes. "
                    + "Acesso restrito a ADMIN e LIBRARIAN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Localização atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou nome duplicado",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Localização não encontrada",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<LocationResponse> update(Long id, LocationRequest request);

    @Operation(summary = "Deleta uma localização",
            description = "Remove permanentemente uma localização do sistema pelo seu ID. Acesso restrito a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Localização deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Localização não encontrada",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado — permissão insuficiente",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<Void> delete(Long id);
}
