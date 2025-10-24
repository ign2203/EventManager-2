package org.example.eventmanagermodule.Events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.eventmanagermodule.Events.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class EventDto {
    private Long id;
    @NotBlank(message = "Validation error: event name cannot be blank")
    @Size(min = 4, max = 32, message = "Event name must be between 4 and 32 characters")
    private String name;
    @NotNull(message = "Owner id should not be null")
    private Long ownerId;
    @NotNull (message = "Location id should not be null")
    private Long locationId;
    @Min(value = 0,message = "The minimum number of seats for an event must not be less than 0")
    @NotNull (message = "max places id should not be null")
    private Integer maxPlaces;
    @Min(value = 0)
    @NotNull(message = "occupied places id should not be null")
    private Integer occupiedPlaces;
    @Future(message = "Event date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal cost;
    @Min(value = 30, message = "The minimum duration of the event should not be less than 30")
    @Max(value = 240, message = "The maximum duration of the event must not exceed 240 minutes")
    private Integer duration;
    private EventStatus status;
}
