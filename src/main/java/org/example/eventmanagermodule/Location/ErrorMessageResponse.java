package org.example.eventmanagermodule.Location;



import java.time.LocalDateTime;


public record ErrorMessageResponse(
        String message,
        String detailedMessage,
        LocalDateTime dateTime
) {
}
