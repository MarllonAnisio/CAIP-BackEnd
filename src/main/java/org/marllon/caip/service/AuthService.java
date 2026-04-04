package org.marllon.caip.service;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.dto.request.UserRequest;
import org.marllon.caip.dto.response.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;




    public void login(String registration, String password){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(registration, password));
    }

    public UserResponse register(UserRequest userRequest){
        return userService.create(userRequest);
    }
}
