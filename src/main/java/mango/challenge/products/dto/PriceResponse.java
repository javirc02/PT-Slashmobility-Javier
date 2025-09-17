package mango.challenge.products.dto;

import lombok.*;
import mango.challenge.products.model.Price;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceResponse {
    private Long id;
    private Long productId;
    private BigDecimal value;
    private LocalDate initDate;
    private LocalDate endDate;

    public PriceResponse(Price price) {
        this.id = price.getId();
        this.productId = price.getProduct().getId();
        this.value = price.getValue();
        this.initDate = price.getInitDate();
        this.endDate = price.getEndDate();
    }
}