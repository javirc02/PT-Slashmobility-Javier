package mango.challenge.products.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mango.challenge.products.dto.PriceRequest;
import mango.challenge.products.dto.PriceResponse;
import mango.challenge.products.service.PriceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    public Page<PriceResponse> getPrices(
            @PathVariable Long productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) BigDecimal minValue,
            @RequestParam(required = false) BigDecimal maxValue,
            @PageableDefault(sort = "init_date", direction = Sort.Direction.DESC) Pageable pageable) {

        return priceService.getPrices(productId, date, fromDate, toDate, minValue, maxValue, pageable);
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