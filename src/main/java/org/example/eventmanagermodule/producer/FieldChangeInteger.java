package org.example.eventmanagermodule.producer;

public record FieldChangeInteger(
      Integer  oldField,
      Integer  newField
) {
}