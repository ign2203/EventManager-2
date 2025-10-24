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
@NoArgsConstructor
@AllArgsConstructor
public class EventUpdateRequestDto {
    @NotBlank
    private String name;
    @NotNull
    private Integer maxPlaces;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private LocalDateTime date;
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal cost;
    @Min(30)
    private Integer duration;
    @NotNull
    Long locationId;
}
