package org.marllon.caip.domains.image.controller.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.marllon.caip.core.exceptions.error.StandardError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "Imagens", description = "Upload e gerenciamento de imagens dos itens reportados via Cloudinary")
public interface ImageControllerDoc {

    @Operation(summary = "Realiza upload de uma imagem",
            description = "Envia uma imagem para o serviço de armazenamento em nuvem (Cloudinary) "
                    + "e retorna a URL pública da imagem. O tamanho máximo do arquivo é 10MB. "
                    + "Qualquer usuário autenticado (STUDENT, LIBRARIAN, ADMIN) pode realizar upload.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upload realizado com sucesso — retorna a URL da imagem"),
            @ApiResponse(responseCode = "400", description = "Arquivo inválido, vazio ou excede o tamanho máximo",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "500", description = "Falha no serviço de armazenamento em nuvem",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<Map<String, String>> uploadImage(MultipartFile file);
}
