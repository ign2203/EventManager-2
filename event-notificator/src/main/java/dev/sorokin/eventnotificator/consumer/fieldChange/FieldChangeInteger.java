package dev.sorokin.eventnotificator.consumer.fieldChange;

public record FieldChangeInteger(
        Integer  oldField,
        Integer  newField
) {
}