package dev.sorokin.eventnotificator.consumer.fieldChange;

import java.time.LocalDateTime;

public record FieldChangeDateTime(
        LocalDateTime oldField,
        LocalDateTime  newField
) {
}