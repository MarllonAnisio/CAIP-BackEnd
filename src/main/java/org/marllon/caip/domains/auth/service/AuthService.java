package org.marllon.caip.domains.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.marllon.caip.domains.auth.dto.request.AuthUserRequest;
import org.marllon.caip.domains.auth.dto.request.RefreshTokenRequest;
import org.marllon.caip.domains.user.dto.request.UserRequest;
import org.marllon.caip.domains.auth.dto.response.AuthUserResponse;
import org.marllon.caip.domains.auth.dto.response.RefreshTokenResponse;
import org.marllon.caip.domains.user.dto.response.UserResponse;
import org.marllon.caip.domains.auth.exceptions.UnauthorizedException;
import org.marllon.caip.core.security.JwtTokenService;
import org.marllon.caip.domains.user.entity.User;
import org.marllon.caip.domains.user.exceptions.IllegalUserActionException;
import org.marllon.caip.domains.user.repository.UserRepository;
import org.marllon.caip.domains.user.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtService;
    private final UserService userService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;


    public AuthUserResponse login(AuthUserRequest authUserRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authUserRequest.registration(), authUserRequest.password())
        );
        String matricula = auth.getName();

        return new AuthUserResponse(
                jwtService.generateAccessToken(matricula),
                jwtService.getAccessExpirationSeconds(),
                matricula,
                jwtService.generateRefreshToken(matricula),
                jwtService.getRefreshExpirationSeconds()
        );
    }

    public UserResponse register(UserRequest userRequest) {
        return userService.create(userRequest);
    }

    public void logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        final String bearer = "Bearer ";

        if (authHeader != null && authHeader.startsWith(bearer)) {
            String token = authHeader.substring(bearer.length());
            try {
                Instant exp = jwtService.extractExpiration(token).toInstant();
                tokenBlacklistService.blacklist(token, exp);
            } catch (Exception e) {
                log.error("Tentativa de logout com token inválido ou já expirado. Detalhes: {}", e.getMessage());
            }
        }
    }

    public RefreshTokenResponse refresh(RefreshTokenRequest requestDto) {
        String refreshToken = requestDto.refreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh Token não fornecido na requisição.");
        }

        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new UnauthorizedException("O Token fornecido foi revogado e não pode mais ser utilizado.");
        }

        try {
            String username = jwtService.extractUsername(refreshToken);

            if (!jwtService.isRefreshTokenValid(refreshToken, username)) {
                throw new UnauthorizedException("O Token fornecido expirou ou é inválido para este usuário.");
            }

            tokenBlacklistService.blacklist(refreshToken, jwtService.extractExpiration(refreshToken).toInstant());

            return new RefreshTokenResponse(
                    jwtService.generateAccessToken(username),
                    jwtService.getAccessExpirationSeconds(),
                    jwtService.generateRefreshToken(username),
                    jwtService.getRefreshExpirationSeconds()
            );

        } catch (Exception e) {
            // log para monitoramento
            log.error("Falha grave na validação do Refresh Token: {}", e.getMessage());
            throw new UnauthorizedException("Falha na validação de segurança do Token.");
        }
    }

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UnauthorizedException("Usuário não autenticado");
        }
        String registration = auth.getName();
        return userRepository.findByRegistration(registration)
                .orElseThrow(() -> new IllegalUserActionException("Usuário autenticado não encontrado"));
    }
}