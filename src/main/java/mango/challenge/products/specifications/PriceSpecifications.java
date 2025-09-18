package mango.challenge.products.specifications;

import mango.challenge.products.model.Price;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PriceSpecifications {
    public static Specification<Price> hasProduct(Long productId) {
        return (root, query, cb) -> cb.equal(root.get("product").get("id"), productId);
    }

    public static Specification<Price> matchesDate(LocalDate date) {
        return (root, query, cb) -> date == null ? null :
                cb.and(
                        cb.lessThanOrEqualTo(root.get("initDate"), date),
                        cb.or(
                                cb.isNull(root.get("endDate")),
                                cb.greaterThanOrEqualTo(root.get("endDate"), date)
                        )
                );
    }

    public static Specification<Price> fromDate(LocalDate fromDate) {
        return (root, query, cb) -> fromDate == null ? null :
                cb.greaterThanOrEqualTo(root.get("initDate"), fromDate);
    }

    public static Specification<Price> toDate(LocalDate toDate) {
        return (root, query, cb) -> toDate == null ? null :
                cb.lessThanOrEqualTo(root.get("endDate"), toDate);
    }

    public static Specification<Price> minValue(BigDecimal minValue) {
        return (root, query, cb) -> minValue == null ? null :
                cb.greaterThanOrEqualTo(root.get("value"), minValue);
    }

    public static Specification<Price> maxValue(BigDecimal maxValue) {
        return (root, query, cb) -> maxValue == null ? null :
                cb.lessThanOrEqualTo(root.get("value"), maxValue);
    }
}
