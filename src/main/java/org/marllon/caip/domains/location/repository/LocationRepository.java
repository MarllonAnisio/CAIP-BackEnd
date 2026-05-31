package org.marllon.caip.domains.location.repository;

import org.marllon.caip.domains.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {

    boolean existsByNameIgnoreCase(String name);
}
