package org.marllon.caip.service;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.dto.request.UserRequest;
import org.marllon.caip.dto.response.UserResponse;
import org.marllon.caip.exception.user_exceptions.IllegalUserActionException;
import org.marllon.caip.model.User;
import org.marllon.caip.model.constants.Role;
import org.marllon.caip.repository.UserRepository;
import org.marllon.caip.service.mapper.UserMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
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
                .orElseThrow(() -> new IllegalUserActionException("Usuário não encontrado"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse create(UserRequest dto) {

        /**
         * Sempre usar a role padrão "Estudante" no auto-registro
         * */

        User user = new User();
        user.setRegistration(dto.registration());
        user.setName(dto.name());

        /**
         * Validação forte de senha: obrigatória, texto puro (não aceitar hash)
         * */
        String rawPassword = dto.password().trim();
        if (isBcrypt(rawPassword)) {
            throw new IllegalUserActionException("Senha deve ser enviada em texto puro, não hash");
        }
        user.setPassword(passwordEncoder.encode(rawPassword));

        /**
         * Ignora qualquer role vinda do cliente no cadastro
         * */
        user.setRole(Role.STUDENT);
        user.setIsActive(true);

        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalUserActionException("Usuário não encontrado"));

        userMapper.updateEntity(user, dto);

        /**
         * Atualiza senha se informada: exige texto puro, não aceita hash pronto
         * */
        if (dto.password() != null && !dto.password().isBlank()) {
            String candidate = dto.password().trim();
            if (isBcrypt(candidate)) {
                throw new IllegalUserActionException("Senha deve ser enviada em texto puro, não hash");
            }
            user.setPassword(passwordEncoder.encode(candidate));
        }

        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalUserActionException("Usuário não encontrado");
        }
        userRepository.deleteById(id);
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
