package org.marllon.caip.repository;

import org.marllon.caip.model.StatusStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusStepRepository extends JpaRepository<StatusStep, Long> {
    Optional<StatusStep> findByName(String statusName);
}
