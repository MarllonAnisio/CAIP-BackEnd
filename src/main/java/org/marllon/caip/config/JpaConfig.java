package org.marllon.caip.config;

import org.marllon.caip.model.User;
import org.marllon.caip.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    private final UserRepository userRepository;

    public JpaConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public AuditorAware<User> auditorProvider() {
        return () -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();

            /**
             * se não ouver ninguem logado retornamos um Optional.empty()
             * */
            if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
                return Optional.empty();
            }

            /**
             * Como o central(chave unica) é a matricula, nosso getName do Authentication é a matricula.
             * */
            String registration = auth.getName();

            /**
             * o spring vai pegar o user e injetar ele no campo @createdBy do AuditInfo.
             * */
            return userRepository.findByRegistration(registration);
        };
    }

}
