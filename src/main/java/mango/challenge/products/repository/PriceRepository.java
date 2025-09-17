package mango.challenge.products.repository;

import mango.challenge.products.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    List<Price> findByProductId(Long productId);

    @Modifying
    @Query("DELETE FROM Price p WHERE p.id = :priceId AND p.product.id = :productId")
    int deleteByIdAndProductId(@Param("priceId") Long priceId, @Param("productId") Long productId);

}
