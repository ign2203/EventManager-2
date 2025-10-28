package org.example.eventmanagermodule.producer;
import java.math.BigDecimal;
public record FieldChangeDecimal(
        BigDecimal  oldField,
        BigDecimal  newField
) {
}