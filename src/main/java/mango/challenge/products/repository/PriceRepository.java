package mango.challenge.products.repository;

import mango.challenge.products.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    List<Price> findByProductId(Long productId);

    @Modifying
    @Query("DELETE FROM Price p WHERE p.id = :priceId AND p.product.id = :productId")
    int deleteByIdAndProductId(@Param("priceId") Long priceId, @Param("productId") Long productId);

    @Query("""
        SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
        FROM Price p
        WHERE p.product.id = :productId
          AND (
                (:endDate IS NULL AND (p.endDate IS NULL OR :initDate <= p.endDate))
                OR
                (:endDate IS NOT NULL AND (
                    (p.endDate IS NULL AND :initDate <= p.initDate)
                    OR NOT (:endDate < p.initDate OR :initDate > p.endDate)
                ))
              )
    """)
    boolean existsOverlappingPrice(@Param("productId") Long productId,
                                   @Param("initDate") LocalDate initDate,
                                   @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT p
        FROM Price p
        WHERE p.product.id = :productId
          AND :date BETWEEN p.initDate AND COALESCE(p.endDate, :date)
    """)
    Optional<Price> findActivePriceAtDate(@Param("productId") Long productId,
                                          @Param("date") LocalDate date);

}
