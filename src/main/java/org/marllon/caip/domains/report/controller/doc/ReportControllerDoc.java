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

import java.util.List;

@Tag(name = "Reports", description = "Gerenciamento de Achados e Perdidos — ciclo completo de criação, vinculação e encerramento de reports")
public interface ReportControllerDoc {

    @Operation(summary = "Cria um novo item reportado",
            description = "Registra um novo report de item perdido ou encontrado. "
                    + "Apenas usuários com role STUDENT podem criar reports. "
                    + "O status inicial é automaticamente atribuído com base no tipo do report (LOST ou FOUND).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item reportado criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos no request",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<ReportResponse> save(ReportRequest request);

    @Operation(summary = "Busca um report por ID",
            description = "Retorna os detalhes completos de um report específico, "
                    + "incluindo localização, status steps, imagem e informações de auditoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report encontrado"),
            @ApiResponse(responseCode = "404", description = "Report não encontrado",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<ReportResponse> findById(Long id);

    @Operation(summary = "Lista meus reports ativos",
            description = "Retorna todos os reports ativos (não fechados) criados pelo usuário autenticado. "
                    + "Acessível por STUDENT, LIBRARIAN e ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reports ativos do usuário")
    })
    ResponseEntity<List<ReportResponse>> getMyReports();

    @Operation(summary = "Lista todos os reports (Staff)",
            description = "Retorna todos os reports do sistema, incluindo ativos e fechados. "
                    + "Acesso restrito a LIBRARIAN e ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista completa de reports"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — permissão insuficiente",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<List<ReportResponse>> findAllForStaff();

    @Operation(summary = "Lista todos os reports ativos",
            description = "Retorna todos os reports que ainda estão abertos (não fechados). "
                    + "Acesso restrito a LIBRARIAN e ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reports ativos")
    })
    ResponseEntity<List<ReportResponse>> findAllActive();

    @Operation(summary = "Lista todos os reports fechados",
            description = "Retorna todos os reports que já foram encerrados (fechados). "
                    + "Acesso restrito a LIBRARIAN e ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reports fechados")
    })
    ResponseEntity<List<ReportResponse>> findAllClosed();

    @Operation(summary = "Atualiza um item reportado",
            description = "Atualiza os dados de um report existente. "
                    + "Não é possível atualizar reports que estejam em status terminal (CONCLUÍDO). "
                    + "Acesso restrito a LIBRARIAN e ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou report em status terminal",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Report não encontrado",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<ReportResponse> update(Long id, ReportRequest request);

    @Operation(summary = "Vincula um report perdido a um encontrado",
            description = "Interliga um item declarado como LOST a um item declarado como FOUND, "
                    + "marcando ambos com o status COMPLETED e fechando-os. "
                    + "Acesso restrito a LIBRARIAN e ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reports vinculados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Reports incompatíveis para vinculação",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Um ou ambos os reports não foram encontrados",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<ReportResponse> linkReports(Long perdidoId, Long encontradoId);

    @Operation(summary = "Lista todos os meus reports",
            description = "Retorna todos os reports (ativos e fechados) criados pelo usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista completa dos meus reports")
    })
    ResponseEntity<List<ReportResponse>> findMyReports();

    @Operation(summary = "Deleta um report (soft delete)",
            description = "Marca o report como deletado (soft delete) e remove a imagem associada do Cloudinary. "
                    + "O dono do report ou LIBRARIAN/ADMIN podem executar esta ação.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Report deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Report não encontrado",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Sem permissão para deletar este report",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<Void> delete(Long id);

    @Operation(summary = "Deleta um report permanentemente (hard delete)",
            description = "Remove definitivamente o report do banco de dados. "
                    + "Esta ação é irreversível. Acesso restrito a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Report deletado permanentemente"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — apenas ADMIN",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<Void> hardDelete(Long id);

    @Operation(summary = "Fecha um report",
            description = "Marca um report como fechado (isClosed = true). "
                    + "Não é possível fechar um report que já está fechado. Acesso restrito a LIBRARIAN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Report fechado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Report já está fechado",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Report não encontrado",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<Void> closeReport(Long id);
}
