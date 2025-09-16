package mango.challenge.products.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mango.challenge.products.dto.PriceDTO;
import mango.challenge.products.service.PriceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/products/{productId}/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;

    @PostMapping
    public ResponseEntity<PriceDTO> addPrice(
            @PathVariable Long productId,
            @Valid @RequestBody PriceDTO priceDTO
    ) {
        PriceDTO created = priceService.addPrice(productId, priceDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PriceDTO>> getPrices(
            @PathVariable Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date != null) {
            PriceDTO priceAtDate = priceService.getPriceAtDate(productId, date);
            return ResponseEntity.ok(List.of(priceAtDate));
        } else {
            List<PriceDTO> prices = priceService.getPrices(productId);
            return ResponseEntity.ok(prices);
        }
    }

    @PatchMapping("/{priceId}")
    public ResponseEntity<PriceDTO> updatePrice(
            @PathVariable Long productId,
            @PathVariable Long priceId,
            @RequestBody PriceDTO priceDTO) {
        PriceDTO updatedPrice = priceService.updatePrice(productId, priceId, priceDTO);
        return ResponseEntity.ok(updatedPrice);
    }

    @DeleteMapping("/{priceId}")
    public ResponseEntity<Void> deletePrice(
            @PathVariable Long productId,
            @PathVariable Long priceId) {
        priceService.deletePrice(productId, priceId);
        return ResponseEntity.noContent().build();
    }
}