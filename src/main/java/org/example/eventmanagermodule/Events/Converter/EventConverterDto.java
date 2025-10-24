package org.example.eventmanagermodule.Events.Converter;

import org.example.eventmanagermodule.Events.Event;
import org.example.eventmanagermodule.Events.dto.EventDto;
import org.springframework.stereotype.Component;

@Component
public class EventConverterDto {
    public EventDto toDto(Event eventDomain) {
        return new EventDto(
                eventDomain.id(),
                eventDomain.name(),
                eventDomain.ownerId(),
                eventDomain.locationId(),
                eventDomain.maxPlaces(),
                eventDomain.occupiedPlaces(),
                eventDomain.date(),
                eventDomain.cost(),
                eventDomain.duration(),
                eventDomain.status()
        );
    }
}
