package org.example.eventmanagermodule.producer.status;

import org.example.eventmanagermodule.Events.EventStatus;


public record EventStatusChangeNotification(
        Long eventId,
        EventStatus oldStatus,
        EventStatus newStatus
) {
}


