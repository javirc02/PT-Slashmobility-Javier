package mango.challenge.products.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mango.challenge.products.dto.PriceRequest;
import mango.challenge.products.dto.PriceResponse;
import mango.challenge.products.service.PriceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/products/{productId}/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;

    @PostMapping
    public ResponseEntity<PriceResponse> addPrice(
            @PathVariable Long productId,
            @Valid @RequestBody PriceRequest priceDTO
    ) {
        PriceResponse created = priceService.addPrice(productId, priceDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PriceResponse>> getPrices(
            @PathVariable Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date != null) {
            PriceResponse priceAtDate = priceService.getPriceAtDate(productId, date);
            return ResponseEntity.ok(List.of(priceAtDate));
        } else {
            List<PriceResponse> prices = priceService.getPrices(productId);
            return ResponseEntity.ok(prices);
        }
    }

    @PatchMapping("/{priceId}")
    public ResponseEntity<PriceResponse> updatePrice(
            @PathVariable Long productId,
            @PathVariable Long priceId,
            @RequestBody PriceRequest priceDTO) {
        PriceResponse updatedPrice = priceService.updatePrice(productId, priceId, priceDTO);
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