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
//
//Фильтр для поиска мероприятий.
// Обязательных полей нет. Если все поля не заданы, то должен вернуться список всех мероприятий
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventSearchRequestDto {



    private String name;




    @Min(value = 5, message = "plaseMin - min = 5")
    private Integer placesMin;

    @Min(5)

 @Nullable
    private Integer placesMax; // количество максимальных мест

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")// нужно подумать, чтобы dateStartAfter был точно меньше dateStartBefore
    private LocalDateTime dateStartAfter;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") // нужно подумать, чтобы dateStartBefore был точно больше dateStartAfter
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
