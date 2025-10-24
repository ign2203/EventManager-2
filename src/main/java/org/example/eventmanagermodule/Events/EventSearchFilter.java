package org.example.eventmanagermodule.Events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventSearchFilter(
        String name,
        Integer placesMin,
        Integer placesMax, // количество максимальных мест
        LocalDateTime dateStartAfter,
        LocalDateTime dateStartBefore,
        BigDecimal costMin,
        BigDecimal costMax,
        Integer durationMin,
        Integer durationMax,
        Long locationId,
        EventStatus status
) {
}
