package org.marllon.caip.domains.user.controller.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.marllon.caip.core.exceptions.error.StandardError;
import org.marllon.caip.domains.user.dto.request.UserRequest;
import org.marllon.caip.domains.user.dto.response.UserResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema CAIP")
public interface UserControllerDoc {

    @Operation(summary = "Obtém o perfil do usuário logado",
            description = "Retorna as informações do usuário autenticado no momento. Qualquer usuário logado pode acessar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<UserResponse> getMyProfile();

    @Operation(summary = "Lista todos os usuários",
            description = "Retorna a lista completa de usuários cadastrados no sistema. Acesso restrito a ADMIN e LIBRARIAN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado — permissão insuficiente",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<List<UserResponse>> findAll();

    @Operation(summary = "Busca um usuário por ID",
            description = "Retorna as informações de um usuário específico pelo seu ID. Acesso restrito a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado — permissão insuficiente",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<UserResponse> findById(Long id);

    @Operation(summary = "Cria um novo usuário (Admin)",
            description = "Permite que um ADMIN force a criação de um usuário. "
                    + "Diferente do registro público, esta rota é administrativa.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado — permissão insuficiente",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<UserResponse> insert(UserRequest dto);

    @Operation(summary = "Atualiza um usuário existente",
            description = "Atualiza as informações de um usuário pelo seu ID. "
                    + "A senha, se informada, deve ser em texto puro. Acesso restrito a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou senha em formato hash",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<UserResponse> update(Long id, UserRequest user);

    @Operation(summary = "Atualiza a role de um usuário",
            description = "Modifica o cargo (role) de um usuário existente. Acesso restrito a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Role inválida",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<UserResponse> updateRole(Long id, org.marllon.caip.domains.user.dto.request.UpdateUserRolesRequest request);

    @Operation(summary = "Deleta um usuário",
            description = "Remove permanentemente um usuário do sistema pelo seu ID. Acesso restrito a ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = StandardError.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado — permissão insuficiente",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<Void> delete(Long id);
}
