package org.marllon.caip.repository;

import org.marllon.caip.model.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {

    boolean existsByNameIgnoreCase(String name);
}
