package org.marllon.caip.config;

import org.marllon.caip.repository.UserRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {

    private final UserRepository userRepository;

    public JpaConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


}
