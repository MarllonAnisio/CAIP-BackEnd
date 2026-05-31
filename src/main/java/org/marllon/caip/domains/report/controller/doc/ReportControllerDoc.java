package org.marllon.caip.domains.report.controller.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.marllon.caip.domains.report.dto.request.ReportRequest;
import org.marllon.caip.domains.report.dto.response.ReportResponse;
import org.marllon.caip.core.exceptions.error.StandardError;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Reports", description = "Gerenciamento de Achados e Perdidos")
public interface ReportControllerDoc {

    @Operation(summary = "Novo item reportado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item reportado criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao criar item reportado",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))


    })
    ResponseEntity<ReportResponse> save(ReportRequest request);


    @GetMapping("/{id}")
    ResponseEntity<ReportResponse> findById(@PathVariable Long id);

    @Operation(summary = "Lista de itens reportados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de itens reportados"),
    })
    ResponseEntity<List<ReportResponse>> getMyReports();

    @Operation(summary = "Lista de itens reportados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de itens reportados"),
    })
    ResponseEntity<List<ReportResponse>> findAllForStaff();


    @Operation(summary = "Lista de itens reportados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de itens reportados"),
    })
    ResponseEntity<List<ReportResponse>> findAllActive();

    @Operation(summary = "Lista de itens reportados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de itens reportados"),
    })
    ResponseEntity<List<ReportResponse>> findAllClosed();


    @Operation(summary = "Atualiza um item reportado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item reportado atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao atualizar item reportado",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<ReportResponse> update(Long id, ReportRequest request);

    @Operation(summary = "Interliga Objetos declarados como perdidos a objetos como encontrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Itens conectados com Sucesso!"),
            @ApiResponse(responseCode = "400", description = "Ocorreu um problema ao interligar itens selecionados")
    })
    ResponseEntity<ReportResponse> linkReports(Long perdidoId, Long encontradoId);

    @GetMapping("/my-reports")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_LIBRARIAN', 'ROLE_ADMIN')")
    ResponseEntity<List<ReportResponse>> findMyReports();

    @Operation(summary = "Deleta um item reportado (soft delete)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item reportado deletado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro ao deletar item reportado",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<Void> delete(Long id);

    @Operation(summary = "Deleta item reportado do banco de dados")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item reportado deletado com sucesso"),
            @ApiResponse(responseCode = "400", description = "erro ao deletar item")
    })
    ResponseEntity<Void> hardDelete(Long id);

    @Operation(summary = "Fecha um item reportado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item reportado fechado com sucesso"),
            @ApiResponse(responseCode = "400", description = "erro ao fechar report")
    })
    ResponseEntity<Void> closeReport(Long id);




}
