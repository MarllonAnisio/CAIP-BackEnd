package org.marllon.caip.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.marllon.caip.dto.request.AuthUserRequest;
import org.marllon.caip.dto.request.RefreshTokenRequest;
import org.marllon.caip.dto.request.UserRequest;
import org.marllon.caip.dto.response.AuthUserResponse;
import org.marllon.caip.dto.response.RefreshTokenResponse;
import org.marllon.caip.dto.response.UserResponse;
import org.marllon.caip.service.AuthService;
import org.marllon.caip.service.TokenBlacklistService;
import org.marllon.caip.service.security.JwtTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(originPatterns = "*")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<AuthUserResponse> login(@RequestBody AuthUserRequest user) throws Exception {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.registration(), user.password())
            );
            String username = auth.getName();
            String accessToken = jwtTokenService.generateAccessToken(username);
            String refreshToken = jwtTokenService.generateRefreshToken(username);

            return ResponseEntity.ok(
                    new AuthUserResponse(
                            accessToken,
                            jwtTokenService.getAccessExpirationSeconds(),
                            username,
                            refreshToken,
                            jwtTokenService.getRefreshExpirationSeconds()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> Register(@RequestBody UserRequest user) throws Exception {
        var userCreated = authService.register(user);
        return ResponseEntity.ok(userCreated);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {

        final String authHeader = request.getHeader("Authorization");
        final String bearer = "Bearer ";
        if (authHeader != null && authHeader.startsWith(bearer)) {
            String token = authHeader.substring(bearer.length());
            try {
                var exp = jwtTokenService.extractExpiration(token).toInstant();
                tokenBlacklistService.blacklist(token, exp);
            } catch (Exception ignored) {}
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@RequestBody RefreshTokenRequest requestDto) {
        String refreshToken = requestDto.refreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        try {
            String username = jwtTokenService.extractUsername(refreshToken);
            if (!jwtTokenService.isRefreshTokenValid(refreshToken, username)) {
                return ResponseEntity.status(401).build();
            }


            tokenBlacklistService.blacklist(refreshToken, jwtTokenService.extractExpiration(refreshToken).toInstant());

            String newAccess = jwtTokenService.generateAccessToken(username);
            String newRefresh = jwtTokenService.generateRefreshToken(username);

            return ResponseEntity.ok(
                    new RefreshTokenResponse(
                            newAccess,
                            jwtTokenService.getAccessExpirationSeconds(),
                            newRefresh,
                            jwtTokenService.getRefreshExpirationSeconds()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
}
