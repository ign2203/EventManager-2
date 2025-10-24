package org.example.eventmanagermodule.producer;
import java.math.BigDecimal;
public record FieldChangeDecimal(
        BigDecimal  oldField, // в спецификации Number, я поставил BigDecimal, так как другие сущности тоже BigDecimal
        BigDecimal  newField
) {
}
