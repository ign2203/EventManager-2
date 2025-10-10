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


    @NotBlank
    String name;
    @NotNull
    Long locationId;
    @Min(0)
    @NotNull
    Integer maxPlaces;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    LocalDateTime date;

    @DecimalMin(value = "0.0", inclusive = true)
    BigDecimal cost;

    @Min(30)
    Integer duration;
}
