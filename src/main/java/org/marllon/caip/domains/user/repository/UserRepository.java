package org.marllon.caip.domains.user.repository;

import org.marllon.caip.domains.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByRegistration(String registration);
    List<User> findAllByRole(String role);

}
