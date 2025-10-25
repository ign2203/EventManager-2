package org.example.eventmanagermodule.producer;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventChangeNotification {
    Long eventId;
    FieldChangeString name;
    FieldChangeInteger maxPlaces;
    FieldChangeDateTime date;
    FieldChangeDecimal cost;
    FieldChangeInteger duration;
    FieldChangeLong locationId;
    FieldChangeString status;
}


