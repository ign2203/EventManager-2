package org.example.eventmanagermodule.Events;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;

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
    @Min(value = 5, message = "plaseMin - min = 5")
    private Integer placesMin;

    @Min(5)
    @Nullable
    private Integer placesMax;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private LocalDateTime dateStartAfter;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private LocalDateTime dateStartBefore;
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal costMin;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal costMax;
    @Min(30)
    private Integer durationMin;
    @Min(30)
    private Integer durationMax;

    private Long locationId;
    private EventStatus status;
}
