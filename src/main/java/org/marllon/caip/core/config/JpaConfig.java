package org.marllon.caip.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuração do mecanismo de auditoria do Spring Data JPA.
 *
 * <p>O {@code auditorAwareRef} aponta para o bean {@link org.marllon.caip.core.security.AuditorAwareImpl},
 * que implementa {@code AuditorAware<User>} e delega a lógica de resolução do usuário
 * corrente ao {@link org.marllon.caip.core.security.SecurityContextService}.</p>
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class JpaConfig {}
