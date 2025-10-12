package org.example.eventmanagermodule.Location;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LocationDto(

        @Null
        Long id,
        @Size(max = 100, message = "Name must not exceed 100 characters")
        @NotBlank(message = "Name cannot be blank")
        String name,
        @NotBlank(message = "Address cannot be blank")
        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,
        @Min(value = 5, message = "Capacity must be at least 5")
        @NotNull
        Integer capacity,
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description
) {
}
