package org.example.eventmanagermodule.Events.Converter;


import org.example.eventmanagermodule.Events.Event;
import org.example.eventmanagermodule.Events.EventEntity;
import org.example.eventmanagermodule.Location.LocationEntity;
import org.example.eventmanagermodule.User.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class EventConverterEntity {

    public Event toDomain (EventEntity entity) {

        return new Event(
                entity.getId(),
                entity.getName(),
                entity.getOwner() != null ? entity.getOwner().getId() : null,
                entity.getLocation() != null ? entity.getLocation().getId() : null,
                entity.getMaxPlaces(),
                entity.getOccupiedPlaces(),
                entity.getDate(),
                entity.getCost(),
                entity.getDuration(),
                entity.getStatus()
        );
    }

    public EventEntity toEntity (Event domain) {
        var user = new UserEntity();
        user.setId(domain.ownerId());

        var location = new LocationEntity();
        location.setId(domain.locationId());

        return new EventEntity(
                domain.id(),
                domain.name(),
                user,
                location,
                domain.maxPlaces(),
                domain.occupiedPlaces(),
                domain.date(),
                domain.cost(),
                domain.duration(),
                domain.status()
        );
    }
}
