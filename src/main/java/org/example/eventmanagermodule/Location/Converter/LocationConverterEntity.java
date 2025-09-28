package org.example.eventmanagermodule.Location.Converter;

import jakarta.persistence.Converter;
import org.example.eventmanagermodule.Location.Location;
import org.example.eventmanagermodule.Location.LocationEntity;
import org.springframework.stereotype.Component;


@Component
public class LocationConverterEntity {

    public Location toDomain(LocationEntity entity) {

        return new Location(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getCapacity(),
                entity.getDescription()
        );
    }

    public LocationEntity toEntity(Location domain) {

        return new LocationEntity(
                domain.id(),
                domain.name(),
                domain.address(),
                domain.capacity(),
                domain.description()
        );
    }
}