package org.example.eventmanagermodule.Location.Converter;

import org.example.eventmanagermodule.Location.Location;
import org.example.eventmanagermodule.Location.LocationDto;
import org.example.eventmanagermodule.Location.LocationEntity;
import org.springframework.stereotype.Component;

@Component
public class LocationConverter {

    public Location fromDtoToDomain(LocationDto dto) {
        return new Location(
                dto.id(),
                dto.name(),
                dto.address(),
                dto.capacity(),
                dto.description()
        );
    }

    public LocationDto fromDomainToDto(Location domain) {
        return new LocationDto(
                domain.id(),
                domain.name(),
                domain.address(),
                domain.capacity(),
                domain.description()
        );
    }

    public Location fromEntityToDomain(LocationEntity entity) {
        return new Location(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getCapacity(),
                entity.getDescription()
        );
    }

    public LocationEntity fromDomainToEntity(Location domain) {
        return new LocationEntity(
                domain.id(),
                domain.name(),
                domain.address(),
                domain.capacity(),
                domain.description()
        );
    }
}