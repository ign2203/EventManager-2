package org.example.eventmanagermodule.Events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.example.eventmanagermodule.Events.EventStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventSearchRequestDto {
    private String name;
    @Min(value = 5, message = "Minimum number of seats (placesMin) must be at least 5")
    private Integer placesMin;
    @Min(value = 5, message = "Maximum number of seats (placesMax) must be at least 5")
    @Nullable
    private Integer placesMax;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private LocalDateTime dateStartAfter;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private LocalDateTime dateStartBefore;
    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum cost (costMin) must not be negative")
    private BigDecimal costMin;
    @DecimalMin(value = "0.0", inclusive = true, message = "Maximum cost (costMax) must not be negative")
    private BigDecimal costMax;
    @Min(value = 30, message = "Minimum event duration (durationMin) must be at least 30 minutes")
    private Integer durationMin;
    @Min(value = 30, message = "Maximum event duration (durationMax) must be at least 30 minutes")
    private Integer durationMax;
    private Long locationId;
    private EventStatus status;
}

