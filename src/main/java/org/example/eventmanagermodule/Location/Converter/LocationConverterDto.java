package org.example.eventmanagermodule.Location.Converter;

import org.example.eventmanagermodule.Location.Location;
import org.example.eventmanagermodule.Location.LocationDto;
import org.springframework.stereotype.Component;

@Component
public class LocationConverterDto {


    public Location toDomain (LocationDto dto) {

        return new Location(
                dto.id(),
                dto.name(),
                dto.address(),
                dto.capacity(),
                dto.description()
        );
    }

    public LocationDto toDto (Location domain) {
        return new LocationDto(
                domain.id(),
                domain.name(),
                domain.address(),
                domain.capacity(),
                domain.description()
        );
    }
}
