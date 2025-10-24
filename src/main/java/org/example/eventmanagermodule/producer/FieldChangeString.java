package org.example.eventmanagermodule.producer;

public record FieldChangeString(
        String oldField,
        String newField
) {
}
