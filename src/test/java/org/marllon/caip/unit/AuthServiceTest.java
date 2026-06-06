package org.marllon.caip.unit;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.marllon.caip.core.security.JwtTokenService;
import org.marllon.caip.domains.auth.dto.request.AuthUserRequest;
import org.marllon.caip.domains.auth.dto.request.RefreshTokenRequest;
import org.marllon.caip.domains.auth.dto.response.AuthUserResponse;
import org.marllon.caip.domains.auth.dto.response.RefreshTokenResponse;
import org.marllon.caip.domains.auth.exceptions.UnauthorizedException;
import org.marllon.caip.domains.auth.service.AuthService;
import org.marllon.caip.domains.auth.service.TokenBlacklistService;
import org.marllon.caip.domains.user.dto.request.UserRequest;
import org.marllon.caip.domains.user.dto.response.UserResponse;
import org.marllon.caip.domains.user.service.UserService;
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

    /**
     * Testa o fluxo principal de autenticação (Login).
     * O objetivo é garantir que, ao receber credenciais válidas, o serviço chama o 
     * AuthenticationManager do Spring Security e, em seguida, utiliza o JwtTokenService 
     * para gerar e retornar um par de tokens (Access e Refresh) corretamente.
     */
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

    /**
     * Testa o registro de novos usuários na camada de autenticação.
     * Como a regra de criação de usuário vive no UserService, este teste valida 
     * apenas se o AuthService está delegando a chamada corretamente para o UserService,
     * evitando duplicação de lógica de negócios.
     */
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

    /**
     * Testa o processo de invalidação de um token durante o logout.
     * Deve extrair o token do cabeçalho "Authorization", verificar sua data de expiração 
     * e passá-lo para o TokenBlacklistService. Isso garante que o token não poderá 
     * ser reutilizado em requisições futuras, mitigando ataques de replay.
     */
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

    /**
     * Testa a resiliência do método de logout.
     * Se o cliente enviar uma requisição de logout sem um token no cabeçalho, 
     * o sistema não deve falhar com NullPointerException, mas sim ignorar a operação 
     * pacificamente, pois não há o que invalidar.
     */
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

    /**
     * Testa o comportamento do sistema perante um token JWT malformado no logout.
     * Se o serviço falhar ao tentar extrair a expiração do token (lançando exceção),
     * a falha deve ser contida num try/catch para não expor erros 500 no endpoint de logout, 
     * apenas logando o incidente.
     */
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

    /**
     * Testa o "Caminho Feliz" da renovação de sessão (Refresh Token).
     * O sistema deve validar o refresh token antigo, gerar um novo par (Access + Refresh)
     * e, CRITICAMENTE, adicionar o refresh token ANTIGO na blacklist (token rotation), 
     * impedindo que ele seja usado para gerar múltiplos acessos infinitamente.
     */
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

    /**
     * Testa o "Caminho Triste" do refresh token quando a payload está vazia.
     * Isso impede que manipulações na API que passem payloads nulas cheguem 
     * a causar NullPointerExceptions na camada de serviço.
     */
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

    /**
     * Teste crucial de segurança: verifica o bloqueio de tokens em blacklist.
     * Se um atacante capturar um refresh token que já foi invalidado (ex: por logout ou token rotation), 
     * o sistema deve rejeitar a operação imeditamente, lançando UnauthorizedException.
     */
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
