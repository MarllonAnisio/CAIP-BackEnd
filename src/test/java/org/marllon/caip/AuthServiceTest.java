package org.marllon.caip;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.marllon.caip.dto.request.AuthUserRequest;
import org.marllon.caip.dto.response.AuthUserResponse;
import org.marllon.caip.service.AuthService;
import org.marllon.caip.service.TokenBlacklistService;
import org.marllon.caip.service.UserService;
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
public class AuthServiceTest {

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
}
