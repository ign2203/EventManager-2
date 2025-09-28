package org.example.eventmanagermodule.Location;


import org.springframework.stereotype.Component;

import java.time.LocalDateTime;


public record ErrorMessageResponse(
        String message,
        String detailedMessage,
        LocalDateTime dateTime
) {
}