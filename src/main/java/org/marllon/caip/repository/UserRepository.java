package org.marllon.caip.repository;

import org.marllon.caip.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByRegistration(String registration);
    List<User> findAllByRole(String role);

}
