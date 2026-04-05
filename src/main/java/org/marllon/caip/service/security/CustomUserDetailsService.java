package org.marllon.caip.service.security;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.model.Role;
import org.marllon.caip.model.User;
import org.marllon.caip.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String registration) throws UsernameNotFoundException {
        User user = userRepository.findByRegistration(registration)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + registration));

        List<GrantedAuthority> authorities = (user.getRoles() == null ? List.<Role>of() : user.getRoles())
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getRegistration()) // usamos registration como principal
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getIsActive() != null && !user.getIsActive())
                .build();
    }
}
