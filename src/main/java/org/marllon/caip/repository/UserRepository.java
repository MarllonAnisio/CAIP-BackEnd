package org.marllon.caip.repository;

import org.marllon.caip.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByRegistration(String registration);
}
