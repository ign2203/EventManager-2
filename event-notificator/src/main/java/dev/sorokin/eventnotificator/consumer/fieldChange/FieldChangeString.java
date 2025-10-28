package dev.sorokin.eventnotificator.consumer.fieldChange;

public record FieldChangeString(
        String oldField,
        String newField
) {
}
