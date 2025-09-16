package mango.challenge.products.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceDTO {
    private Long id;
    private BigDecimal value;
    private LocalDate initDate;
    private LocalDate endDate;
}