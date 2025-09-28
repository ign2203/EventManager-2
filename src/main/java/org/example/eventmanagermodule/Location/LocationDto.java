package org.example.eventmanagermodule.Location;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LocationDto(

        @Null
        Long id,

        @Size(min = 1) // не уверен
        @NotBlank
        String name,

        @NotBlank
        String address,

        @Min(5)// по ТЗ
        @NotNull
        Integer capacity,

        String description
) {
}
