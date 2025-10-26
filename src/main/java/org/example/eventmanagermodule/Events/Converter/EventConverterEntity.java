package org.example.eventmanagermodule.Events.Converter;

import org.example.eventmanagermodule.Events.Event;
import org.example.eventmanagermodule.Events.EventEntity;
import org.example.eventmanagermodule.Location.LocationEntity;
import org.example.eventmanagermodule.User.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class EventConverterEntity {
    public Event toDomain(EventEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("EventEntity не может быть null");
        }
        Long ownerId = null;
        if (entity.getOwner() != null) {
            ownerId = entity.getOwner().getId();
        }
        Long locationId = null;
        if (entity.getLocation() != null) {
            locationId = entity.getLocation().getId();
        }
        return new Event(
                entity.getId(),
                entity.getName(),
                ownerId,
                locationId,
                entity.getMaxPlaces(),
                entity.getOccupiedPlaces(),
                entity.getDate(),
                entity.getCost(),
                entity.getDuration(),
                entity.getStatus()
        );
    }
    public EventEntity toEntity(Event domain) {
        if (domain == null) {
            throw new IllegalArgumentException("EventDomain не может быть null");
        }
        UserEntity user = null;
        if ((domain.ownerId() != null)) {
            user = new UserEntity();
            user.setId(domain.ownerId());
        }
        LocationEntity locationEntity = null;
        if ((domain.locationId() != null)) {
            locationEntity = new LocationEntity();
            locationEntity.setId(domain.locationId());
        }
        return new EventEntity(
                domain.id(),
                domain.name(),
                user,
                locationEntity,
                domain.maxPlaces(),
                domain.occupiedPlaces(),
                domain.date(),
                domain.cost(),
                domain.duration(),
                domain.status()
        );
    }
}