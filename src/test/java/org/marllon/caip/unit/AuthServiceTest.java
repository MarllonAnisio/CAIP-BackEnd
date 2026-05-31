package org.marllon.caip.unit;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marllon.caip.dto.request.AuthUserRequest;
import org.marllon.caip.dto.request.RefreshTokenRequest;
import org.marllon.caip.dto.request.UserRequest;
import org.marllon.caip.dto.response.AuthUserResponse;
import org.marllon.caip.dto.response.RefreshTokenResponse;
import org.marllon.caip.dto.response.UserResponse;
import org.marllon.caip.exception.auth_exceptions.UnauthorizedException;
import org.marllon.caip.service.impl.AuthService;
import org.marllon.caip.service.impl.TokenBlacklistService;
import org.marllon.caip.service.impl.UserService;
import org.marllon.caip.service.security.JwtTokenService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
 class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock private JwtTokenService jwtService;
    @Mock private UserService userService;
    @Mock private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Deve realizar login com sucesso e retornar Access e Refresh Tokens")
    void quandoLoginComSucesso_deveRetornarTokens() {

        AuthUserRequest request = new AuthUserRequest("123456", "senhaForte123");

        Authentication authMock = mock(Authentication.class);
        when(authMock.getName()).thenReturn("123456");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authMock);

        when(jwtService.generateAccessToken("123456")).thenReturn("access-token-falso");
        when(jwtService.generateRefreshToken("123456")).thenReturn("refresh-token-falso");
        when(jwtService.getAccessExpirationSeconds()).thenReturn(900L);
        when(jwtService.getRefreshExpirationSeconds()).thenReturn(604800L);

        AuthUserResponse response = authService.login(request);

        assertNotNull(response);

        assertEquals("access-token-falso", response.token());
        assertEquals(900L, response.expiresInSeconds());
        assertEquals("123456", response.usernameOrEmail());
        assertEquals("refresh-token-falso", response.refreshToken());
        assertEquals(604800L, response.refreshExpirationSeconds());

        verify(authenticationManager, times(1)).authenticate(any());
    }
    // ====================================================================
    // TESTES DE REGISTER
    // ====================================================================

    @Test
    @DisplayName("Deve registrar usuário delegando para o UserService")
    void quandoRegister_deveChamarUserService() {
        // Arrange
        UserRequest request = mock(UserRequest.class); // Mock rápido pq a gente não precisa de um UserRequest real
        UserResponse expectedResponse = mock(UserResponse.class);

        when(userService.create(request)).thenReturn(expectedResponse);

        // Act
        UserResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(userService, times(1)).create(request);
    }


    // ====================================================================
    // TESTES DE LOGOUT
    // ====================================================================

    @Test
    @DisplayName("Deve extrair o token do request e colocar na blacklist")
    void quandoLogoutComTokenValido_deveAdicionarNaBlacklist() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "token.jwt.valido";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        java.util.Date dataExpiracao = new java.util.Date();
        when(jwtService.extractExpiration(token)).thenReturn(dataExpiracao);

        // Act
        authService.logout(request);

        // Assert
        verify(tokenBlacklistService, times(1)).blacklist(token, dataExpiracao.toInstant());
    }

    @Test
    @DisplayName("Não deve fazer nada se o header Authorization for nulo no Logout")
    void quandoLogoutSemHeader_naoDeveFazerNada() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        authService.logout(request);

        // Assert
        // Garante que o serviço de blacklist NUNCA foi chamado
        verify(tokenBlacklistService, times(0)).blacklist(any(), any());
    }

    @Test
    @DisplayName("Deve capturar exceção no Logout se o token for inválido e não quebrar a API")
    void quandoLogoutComTokenInvalido_deveLogarErroENaoQuebrar() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "token.invalido";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // Forçamos o jwtService a dar erro ao tentar ler um token bizarro
        when(jwtService.extractExpiration(token)).thenThrow(new RuntimeException("Token malformado"));

        // Act & Assert
        // org.junit.jupiter.api.Assertions.assertDoesNotThrow garante que a exceção foi capturada no try/catch
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> authService.logout(request));
    }

    // ====================================================================
    // TESTES DE REFRESH TOKEN
    // ====================================================================

    @Test
    @DisplayName("Deve gerar novos tokens se o refresh token for válido")
    void quandoRefreshValido_deveRetornarNovosTokens() {
        // Arrange
        String oldRefreshToken = "refresh-antigo";
        RefreshTokenRequest request = new RefreshTokenRequest(oldRefreshToken);
        String username = "123456";
        java.util.Date dataExpiracao = new java.util.Date();

        when(tokenBlacklistService.isBlacklisted(oldRefreshToken)).thenReturn(false);
        when(jwtService.extractUsername(oldRefreshToken)).thenReturn(username);
        when(jwtService.isRefreshTokenValid(oldRefreshToken, username)).thenReturn(true);
        when(jwtService.extractExpiration(oldRefreshToken)).thenReturn(dataExpiracao);

        when(jwtService.generateAccessToken(username)).thenReturn("novo-access");
        when(jwtService.generateRefreshToken(username)).thenReturn("novo-refresh");
        when(jwtService.getAccessExpirationSeconds()).thenReturn(900L);
        when(jwtService.getRefreshExpirationSeconds()).thenReturn(604800L);

        // Act
        RefreshTokenResponse response = authService.refresh(request);

        // Assert
        assertNotNull(response);
        assertEquals("novo-access", response.accessToken());
        assertEquals("novo-refresh", response.refreshToken());

        // Verifica se o token antigo foi inutilizado!
        verify(tokenBlacklistService, times(1)).blacklist(oldRefreshToken, dataExpiracao.toInstant());
    }

    @Test
    @DisplayName("Deve lançar erro se o Refresh Token for nulo ou vazio")
    void quandoRefreshNuloOuVazio_deveLancarUnauthorized() {
        // Arrange
        RefreshTokenRequest requestNulo = new RefreshTokenRequest(null);
        RefreshTokenRequest requestVazio = new RefreshTokenRequest("   ");

        // Act & Assert (espera que lance sua exceção customizada)
        org.junit.jupiter.api.Assertions.assertThrows(UnauthorizedException.class, () -> authService.refresh(requestNulo));
        org.junit.jupiter.api.Assertions.assertThrows(UnauthorizedException.class, () -> authService.refresh(requestVazio));
    }

    @Test
    @DisplayName("Deve lançar erro se o Refresh Token já estiver na blacklist")
    void quandoRefreshNaBlacklist_deveLancarUnauthorized() {
        // Arrange
        String blacklistedToken = "token-roubado";
        RefreshTokenRequest request = new RefreshTokenRequest(blacklistedToken);

        when(tokenBlacklistService.isBlacklisted(blacklistedToken)).thenReturn(true);

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(UnauthorizedException.class, () -> authService.refresh(request));
    }

}