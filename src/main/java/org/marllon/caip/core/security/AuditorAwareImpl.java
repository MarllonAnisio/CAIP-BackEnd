package org.marllon.caip.core.security;

import io.micrometer.common.lang.NonNull;
import org.marllon.caip.domains.user.entity.User;
import org.marllon.caip.domains.user.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class AuditorAwareImpl {

    private UserRepository userRepository;

    @NonNull
    public Optional<User> getCurrentAuditor() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        String registration = auth.getName();
        return userRepository.findByRegistration(registration);
    }
}
