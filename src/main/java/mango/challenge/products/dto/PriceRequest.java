package mango.challenge.products.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceRequest {

    @NotNull(message = "El valor no puede ser nulo")
    @DecimalMin(value = "0.0", message = "El valor debe ser mayor que 0")
    private BigDecimal value;

    @NotNull(message = "La fecha de inicio no puede ser nula")
    private LocalDate initDate;

    private LocalDate endDate;

    @AssertTrue(message = "initDate debe ser menor que endDate")
    private boolean isValidDateRange() {
        return endDate == null || initDate.isBefore(endDate) || initDate.isEqual(endDate);
    }
}