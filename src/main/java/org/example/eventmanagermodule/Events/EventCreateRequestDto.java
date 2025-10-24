package org.example.eventmanagermodule.Events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventCreateRequestDto {
    @NotBlank(message = "Name cannot be blank")
    String name;
    @NotNull(message = "location_id cannot be null")
    Long locationId;
    @Min(value = 0, message = "The minimum number of seats for an event must not be less than 0")
    @NotNull(message = "maxPlaces cannot be null")
    Integer maxPlaces;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    LocalDateTime date;
    @DecimalMin(value = "0.0", inclusive = true)
    BigDecimal cost;
    @Min(value = 30, message = "The minimum duration of the event should not be less than 30")
    Integer duration;
}
