package org.marllon.caip.domains.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.marllon.caip.domains.auth.dto.request.AuthUserRequest;
import org.marllon.caip.domains.auth.dto.request.RefreshTokenRequest;
import org.marllon.caip.domains.user.dto.request.UserRequest;
import org.marllon.caip.domains.auth.dto.response.AuthUserResponse;
import org.marllon.caip.domains.auth.dto.response.RefreshTokenResponse;
import org.marllon.caip.domains.user.dto.response.UserResponse;
import org.marllon.caip.domains.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthUserResponse> login(@RequestBody AuthUserRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }
}
