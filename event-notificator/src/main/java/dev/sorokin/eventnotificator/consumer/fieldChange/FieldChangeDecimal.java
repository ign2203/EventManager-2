package dev.sorokin.eventnotificator.consumer.fieldChange;
import java.math.BigDecimal;
public record FieldChangeDecimal(
        BigDecimal  oldField,
        BigDecimal  newField
) {
}
