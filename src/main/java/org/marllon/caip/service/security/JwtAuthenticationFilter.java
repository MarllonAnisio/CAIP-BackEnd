package org.marllon.caip.service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.marllon.caip.exception.auth_exceptions.UnauthorizedException;
import org.marllon.caip.service.TokenBlacklistService;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor // O Sênior usa Lombok para criar o construtor automaticamente
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    // 👇 O Mensageiro: Ele pega erros dos Filtros e joga para o seu GlobalExceptionHandler
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String bearerPrefix = "Bearer ";

        try {
            if (authHeader != null && authHeader.startsWith(bearerPrefix)) {
                String token = authHeader.substring(bearerPrefix.length());

                // Barreira da Blacklist
                if (tokenBlacklistService.isBlacklisted(token)) {
                    throw new UnauthorizedException("Token revogado. Faça login novamente.");
                }
                // Barreira do Tipo de Token
                if (!jwtTokenService.isAccessToken(token)) {
                    throw new UnauthorizedException("Espera-se um Access Token, mas outro tipo foi fornecido.");
                }

                // 3. Extração (Se o token estiver expirado)
                String username = jwtTokenService.extractUsername(token);

                // 4. Autenticação no Contexto
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                    // aqui eu to criando o token de acesso
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // esse log ta servindo para segurança e saúde da aplicação
            log.error("Erro de segurança bloqueado no Filtro JWT: {}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.startsWith("/auth/");
    }
}