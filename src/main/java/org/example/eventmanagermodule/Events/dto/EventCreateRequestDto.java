package org.example.eventmanagermodule.Events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventCreateRequestDto {
    @NotBlank(message = "Validation error: event name cannot be blank")
    String name;
    @NotNull(message = "location id cannot be null")
    Long locationId;
    @Min(value = 0, message = "The minimum number of seats for an event must not be less than 0")
    @NotNull(message = "Max places cannot be null")
    Integer maxPlaces;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @Future(message = "Event date must be in the future")
    LocalDateTime date;
    @DecimalMin(value = "0.0", inclusive = true)
    BigDecimal cost;
    @Min(value = 30, message = "The minimum duration of the event should not be less than 30")
    @Max(value = 240, message = "The maximum duration of the event must not exceed 240 minutes")
    Integer duration;
}