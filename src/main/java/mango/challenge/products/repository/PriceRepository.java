package mango.challenge.products.repository;

import mango.challenge.products.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    List<Price> findByProductId(Long productId);

    List<Price> findByProductIdAndInitDateLessThanEqualAndEndDateGreaterThanEqualOrEndDateIsNull(
            Long productId, LocalDate date1, LocalDate date2
    );
}
