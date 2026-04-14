package org.marllon.caip.service;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.dto.request.UserRequest;
import org.marllon.caip.dto.response.AuthUserResponse;
import org.marllon.caip.dto.response.UserResponse;
import org.marllon.caip.service.security.JwtTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtService;
    private final UserService userService;

    public AuthUserResponse login(String registration, String password){
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registration, password)
        );
        String matricula = auth.getName();

        String accessToken = jwtService.generateAccessToken(matricula);
        String refreshToken = jwtService.generateRefreshToken(matricula);

        return new AuthUserResponse(
                accessToken,
                jwtService.getAccessExpirationSeconds(),
                matricula,
                refreshToken,
                jwtService.getRefreshExpirationSeconds()
        );
    }
    public UserResponse register(UserRequest userRequest){
        return userService.create(userRequest);
    }
}
