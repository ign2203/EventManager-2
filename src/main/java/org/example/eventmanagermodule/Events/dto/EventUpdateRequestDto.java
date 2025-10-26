package org.example.eventmanagermodule.Events.dto;

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
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateRequestDto {
    @NotBlank(message = "Event name must not be blank")
    private String name;
    @NotNull(message = "Maximum number of seats (maxPlaces) cannot be null")
    private Integer maxPlaces;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private LocalDateTime date;
    @DecimalMin(value = "0.0", inclusive = true, message = "Event cost must not be negative")
    private BigDecimal cost;
    @Min(value = 30, message = "Event duration must be at least 30 minutes")
    private Integer duration;
    @NotNull(message = "Location ID cannot be null")
    private Long locationId;
}