package org.example.eventmanagermodule.eventmanager;

import java.time.LocalDateTime;

public record FieldChangeDateTime(
        LocalDateTime oldField,
        LocalDateTime  newField
) {
}
