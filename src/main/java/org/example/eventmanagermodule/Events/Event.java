package org.example.eventmanagermodule.Events;




import java.math.BigDecimal;
import java.time.LocalDateTime;


public record Event(
        Long id,
        String name,
        Long ownerId,
        Long locationId,
        Integer maxPlaces,
        Integer occupiedPlaces,
        LocalDateTime date,
        BigDecimal cost,
        Integer duration,
        EventStatus status

) {
}
