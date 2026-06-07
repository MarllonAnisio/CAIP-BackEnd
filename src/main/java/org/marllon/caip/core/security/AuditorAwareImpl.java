package org.marllon.caip.core.security;

import lombok.RequiredArgsConstructor;
import org.marllon.caip.domains.user.entity.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Implementação do contrato {@link AuditorAware} do Spring Data JPA.
 *
 * <p>É registrada como bean pelo {@code @EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")}
 * em {@link org.marllon.caip.core.config.JpaConfig} e é invocada automaticamente pelo
 * Spring sempre que uma entidade com {@code @CreatedBy} ou {@code @LastModifiedBy}
 * é persistida ou atualizada.</p>
 *
 * <p>Delega a lógica real para o {@link SecurityContextService}, garantindo
 * que a obtenção do usuário autenticado esteja centralizada em um único lugar.</p>
 */
@Component
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<User> {

    private final SecurityContextService securityContextService;

    @Override
    @NonNull
    public Optional<User> getCurrentAuditor() {
        return securityContextService.getOptionalAuthenticatedUser();
    }
}
