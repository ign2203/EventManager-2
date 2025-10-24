package org.example.eventmanagermodule.Location;

public record Location(
        Long id,
        String name,
        String address,
        Integer capacity,//вместимость
        String description
) {
}