package org.example.eventmanagermodule.Location;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LocationDto(

        @Null
        Long id,

        @Size(min = 1) // не уверен
        @NotBlank(message = "Location name should be not blank")
        String name,

        @NotBlank (message = "Location address should be not blank")
        String address,

        @Min(value = 5, message = "Minimum capacity of location is 5")
        @NotNull
        Integer capacity,

        String description
) {
}
