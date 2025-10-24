package org.example.eventmanagermodule.Events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class EventDto {
    private Long id;
    @NotBlank
    private String name;
    @NotNull
    private Long ownerId;
    @NotNull
    private Long locationId;
    @Min(0)
    @NotNull
    private Integer maxPlaces;
    @Min(0)
    @NotNull
    private Integer occupiedPlaces;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal cost;
    @Min(30)
    private Integer duration;
    private EventStatus status;
}
