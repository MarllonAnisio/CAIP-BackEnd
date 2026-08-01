package org.marllon.caip.domains.auth.controller.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.marllon.caip.core.exceptions.error.StandardError;
import org.marllon.caip.domains.auth.dto.request.AuthUserRequest;
import org.marllon.caip.domains.auth.dto.request.RefreshTokenRequest;
import org.marllon.caip.domains.auth.dto.response.AuthUserResponse;
import org.marllon.caip.domains.auth.dto.response.RefreshTokenResponse;
import org.marllon.caip.domains.user.dto.request.UserRequest;
import org.marllon.caip.domains.user.dto.response.UserResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Autenticação", description = "Endpoints de autenticação, registro, logout e renovação de tokens JWT")
public interface AuthControllerDoc {

    @Operation(summary = "Realiza login no sistema",
            description = "Autentica o usuário com matrícula e senha, retornando um access token e um refresh token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<AuthUserResponse> login(AuthUserRequest request);

    @Operation(summary = "Registra um novo usuário",
            description = "Cria uma nova conta de estudante no sistema. A role padrão é STUDENT, independente do que for enviado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de registro inválidos",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<UserResponse> register(UserRequest request);

    @Operation(summary = "Realiza logout do sistema",
            description = "Invalida o token JWT atual adicionando-o a uma blacklist. "
                    + "O token não poderá mais ser utilizado para autenticação.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou já expirado",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<Void> logout(HttpServletRequest request);

    @Operation(summary = "Renova os tokens de acesso",
            description = "Recebe um refresh token válido e retorna um novo par de access token e refresh token. "
                    + "O refresh token antigo é invalidado (rotação de tokens).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido, expirado ou revogado",
                    content = @Content(schema = @Schema(implementation = StandardError.class)))
    })
    ResponseEntity<RefreshTokenResponse> refresh(RefreshTokenRequest request);
}
