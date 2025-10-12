package org.example.eventmanagermodule.Location;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    boolean existsByName(String name);

    boolean existsById(Long id);
}
