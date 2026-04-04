package org.marllon.caip.repository;

import org.marllon.caip.model.StatusStep;

import java.util.Optional;

public interface StatusStepRepository {
    Optional<StatusStep> findByName(String statusName);
}
