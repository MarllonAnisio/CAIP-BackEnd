package org.marllon.caip.controller.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.marllon.caip.dto.request.ReportRequest;
import org.marllon.caip.dto.response.ReportResponse;
import org.marllon.caip.exception.StandardError;
import org.springframework.http.ResponseEntity;

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


    @Operation(summary = "Lista de itens reportados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de itens reportados"),
    })
    public ResponseEntity<List<ReportResponse>> getMyReports();




}
