package org.example.eventmanagermodule.producer;

import java.time.LocalDateTime;

public record FieldChangeDateTime(
        LocalDateTime oldField,
        LocalDateTime  newField
) {
}