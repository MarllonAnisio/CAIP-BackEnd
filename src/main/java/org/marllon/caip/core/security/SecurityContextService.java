package org.marllon.caip.core.security;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.domains.auth.exceptions.UnauthorizedException;
import org.marllon.caip.domains.user.entity.User;
import org.marllon.caip.domains.user.exceptions.IllegalUserActionException;
import org.marllon.caip.domains.user.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Serviço central responsável por expor o contexto de segurança da aplicação.
 *
 * <p>Concentra toda a lógica de leitura do {@link SecurityContextHolder} em um único lugar,
 * eliminando a necessidade de outros services (AuthService, ReportService, etc.)
 * acessarem diretamente o {@code SecurityContextHolder} ou o {@code UserRepository}
 * para identificar quem está autenticado.</p>
 *
 * <p>Também é a implementação real utilizada pelo {@link AuditorAwareImpl}
 * para o mecanismo de auditoria do Spring Data JPA.</p>
 */
@Service
@RequiredArgsConstructor
public class SecurityContextService {

    private final UserRepository userRepository;

    /**
     * Retorna o usuário autenticado na requisição corrente.
     *
     * @return o {@link User} correspondente à autenticação ativa.
     * @throws UnauthorizedException      se não houver autenticação ativa.
     * @throws IllegalUserActionException se o registro no token não existir no banco.
     */
    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("Usuário não autenticado.");
        }

        String registration = auth.getName();
        return userRepository.findByRegistration(registration)
                .orElseThrow(() -> new IllegalUserActionException(
                        "Usuário autenticado com matrícula '" + registration + "' não encontrado no sistema."));
    }

    /**
     * Retorna o usuário autenticado encapsulado em um {@link Optional}.
     * Retorna {@link Optional#empty()} quando não há sessão ativa (contexto anônimo).
     * Utilizado pelo mecanismo de auditoria JPA.
     *
     * @return {@code Optional<User>} com o usuário, ou vazio se não autenticado.
     */
    public Optional<User> getOptionalAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        return userRepository.findByRegistration(auth.getName());
    }
}
