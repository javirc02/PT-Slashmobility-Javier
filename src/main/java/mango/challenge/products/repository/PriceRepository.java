package mango.challenge.products.repository;

import mango.challenge.products.model.Price;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    @Modifying
    @Query("DELETE FROM Price p WHERE p.id = :priceId AND p.product.id = :productId")
    int deleteByIdAndProductId(@Param("priceId") Long priceId, @Param("productId") Long productId);

    @Query(value = """
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM prices p
        WHERE p.product_id = :productId
          AND (
                (CAST(:initDate AS DATE) <= p.end_date OR p.end_date IS NULL)
            AND (CAST(:endDate AS DATE) >= p.init_date OR CAST(:endDate AS DATE) IS NULL)
          )
    """, nativeQuery = true)
    boolean existsOverlappingPrice(@Param("productId") Long productId,
                                   @Param("initDate") LocalDate initDate,
                                   @Param("endDate") LocalDate endDate);


    @Query(value = """
        SELECT p.*
        FROM prices p
        WHERE p.product_id = :productId
          AND (CAST(:date AS DATE) IS NULL OR
               (p.init_date <= CAST(:date AS DATE) AND
               (p.end_date IS NULL OR CAST(:date AS DATE) <= p.end_date)))
          AND (CAST(:fromDate AS DATE) IS NULL OR p.init_date >= CAST(:fromDate AS DATE))
          AND (CAST(:toDate AS DATE) IS NULL OR
               (p.end_date IS NOT NULL AND p.end_date <= CAST(:toDate AS DATE)))
          AND (CAST(:minValue AS DECIMAL) IS NULL OR p.value >= CAST(:minValue AS DECIMAL))
          AND (CAST(:maxValue AS DECIMAL) IS NULL OR p.value <= CAST(:maxValue AS DECIMAL))
    """, nativeQuery = true)
    Page<Price> findByProductWithFilters(
            @Param("productId") Long productId,
            @Param("date") LocalDate date,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("minValue") BigDecimal minValue,
            @Param("maxValue") BigDecimal maxValue,
            Pageable pageable
    );

}
