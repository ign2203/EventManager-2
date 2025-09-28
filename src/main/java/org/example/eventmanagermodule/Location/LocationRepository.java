package org.example.eventmanagermodule.Location;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {


    boolean existsByName(String name);

    boolean existsById(Long id);


    @Transactional
    @Modifying
    @Query("""
            UPDATE LocationEntity locationEntity
            SET 
                    locationEntity.name = :name,
                  locationEntity.address = :address,
                    locationEntity.capacity =:capacity,
                  locationEntity.description =:description
            WHERE locationEntity.id =:locationId 
            """)
    void updateLocation(
            @Param("locationId") Long id,
            @Param("name") String name,
            @Param("address") String address,
            @Param("capacity") Integer capacity,
            @Param("description") String description
    );
}
