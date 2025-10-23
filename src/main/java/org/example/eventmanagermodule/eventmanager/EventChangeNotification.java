package org.example.eventmanagermodule.eventmanager;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class EventChangeNotification {
    Long eventId;
    FieldChangeString name;
    FieldChangeInteger maxPlaces;
    FieldChangeDateTime date;
    FieldChangeDecimal cost;
    FieldChangeInteger duration;
    FieldChangeLong locationId;
}


