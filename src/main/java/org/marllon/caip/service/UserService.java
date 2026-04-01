package org.marllon.caip.service;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.dto.request.UserRequest;
import org.marllon.caip.dto.response.UserResponse;
import org.marllon.caip.model.Role;
import org.marllon.caip.model.User;
import org.marllon.caip.repository.RoleRepository;
import org.marllon.caip.repository.UserRepository;
import org.marllon.caip.service.mapper.UserMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse create(UserRequest dto) {

        /*Sempre usar a role padrão "Estudante" no auto-registro*/
        Role role = roleRepository.findByName("Estudante")
                .orElseThrow(() -> new IllegalStateException("Role padrão 'Estudante' não encontrada"));

        User user = userMapper.toEntity(dto);

        // Validação forte de senha: obrigatória, texto puro (não aceitar hash)
        String rawPassword = dto.password() == null ? "" : dto.password().trim();
        if (rawPassword.isBlank()) {
            throw new IllegalArgumentException("Senha é obrigatória");
        }
        if (isBcrypt(rawPassword)) {
            throw new IllegalArgumentException("Senha deve ser enviada em texto puro, não hash");
        }
        user.setPassword(passwordEncoder.encode(rawPassword));

        /* Ignora qualquer role vinda do cliente no cadastro
         *  */
        user.setRoles(List.of(role));
        user.setActive(true);

        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));


        userMapper.updateEntity(user, dto);

        // Atualiza senha se informada: exige texto puro, não aceita hash pronto

        if (dto.password() != null && !dto.password().isBlank()) {
            String candidate = dto.password().trim();
            if (isBcrypt(candidate)) {
                throw new IllegalArgumentException("Senha deve ser enviada em texto puro, não hash");
            }
            user.setPassword(passwordEncoder.encode(candidate));
        }

        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateRoles(Long userId, List<String> roleNames) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        List<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new IllegalArgumentException("Role não encontrada: " + name)))
                .toList();

        user.setRoles(roles);
        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    private Role getRoleOrDefault(Role roleInput) {
        if (roleInput != null && roleInput.getId() != null) {
            return roleRepository.findById(roleInput.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Papel (Role) não encontrado"));
        }
        return roleRepository.findByName("Estudante")
                .orElseThrow(() -> new IllegalStateException("Role padrão 'Estudante' não encontrado"));
    }

    // Detecta se a string parece ser um hash BCrypt ($2a, $2b, $2y)
    private boolean isBcrypt(String s) {
        return s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$");
    }

    // Novo método: retorna o usuário autenticado (perfil)
    @Transactional(readOnly = true)
    public UserResponse getAuthenticatedUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            throw new IllegalArgumentException("Usuário não autenticado");
        }
        String registration = auth.getName(); // no login, o principal é a registration
        User user = userRepository.findByRegistration(registration)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
        return userMapper.toResponse(user);
    }
}
