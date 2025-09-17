package mango.challenge.products.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mango.challenge.products.dto.PriceRequest;
import mango.challenge.products.dto.PriceResponse;
import mango.challenge.products.exception.ResourceNotFoundException;
import mango.challenge.products.model.Price;
import mango.challenge.products.model.Product;
import mango.challenge.products.repository.PriceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PriceService {

    private final PriceRepository priceRepository;
    private final ProductService productService;

    public PriceResponse addPrice(Long productId, PriceRequest priceRequest) {
        Product product = productService.getProductByIdOrThrow(productId);

        // Validación de fechas
        if (priceRequest.getEndDate() != null && priceRequest.getInitDate().isAfter(priceRequest.getEndDate())) {
            throw new IllegalArgumentException("initDate debe ser menor que endDate");
        }

        if (priceRepository.existsOverlappingPrice(
                productId,
                priceRequest.getInitDate(),
                priceRequest.getEndDate()
        )) {
            throw new IllegalArgumentException("El rango de fechas se solapa con otro precio existente");
        }

        return new PriceResponse(priceRepository.save(new Price(priceRequest, product)));
    }

    public List<PriceResponse> getPrices(Long productId) {
        productService.getProductByIdOrThrow(productId);

        return priceRepository.findByProductId(productId).stream()
                .map(PriceResponse::new)
                .collect(Collectors.toList());
    }

    public PriceResponse getPriceAtDate(Long productId, LocalDate date) {
        productService.getProductByIdOrThrow(productId);

        return priceRepository.findActivePriceAtDate(productId, date)
                .map(PriceResponse::new)
                .orElseThrow(() -> new IllegalArgumentException("No hay precio vigente para esta fecha"));
    }

    public PriceResponse updatePrice(Long productId, Long priceId, PriceRequest priceRequest) {
        Product product = productService.getProductByIdOrThrow(productId);

        Price existingPrice = priceRepository.findById(priceId)
                .orElseThrow(() -> new ResourceNotFoundException("Precio no encontrado"));

        if (!product.getPrices().contains(existingPrice)) {
            throw new ResourceNotFoundException("El precio no pertenece al producto");
        }

        if (priceRequest.getValue() != null) {
            existingPrice.setValue(priceRequest.getValue());
        }
        if (priceRequest.getInitDate() != null) {
            existingPrice.setInitDate(priceRequest.getInitDate());
        }
        if (priceRequest.getEndDate() != null) {
            existingPrice.setEndDate(priceRequest.getEndDate());
        }

        if (existingPrice.getEndDate() != null && existingPrice.getInitDate().isAfter(existingPrice.getEndDate())) {
            throw new IllegalArgumentException("initDate debe ser menor que endDate");
        }

        boolean overlap = product.getPrices().stream()
                .filter(p -> !p.getId().equals(priceId))
                .anyMatch(p ->
                        (existingPrice.getEndDate() == null || p.getEndDate() == null
                                ? existingPrice.getInitDate().isBefore(p.getEndDate() != null ? p.getEndDate().plusDays(1) : LocalDate.MAX)
                                : !(existingPrice.getEndDate().isBefore(p.getInitDate()) || existingPrice.getInitDate().isAfter(p.getEndDate())))
                );
        if (overlap) {
            throw new IllegalArgumentException("El rango de fechas se solapa con otro precio existente");
        }

        return new PriceResponse(priceRepository.save(existingPrice));
    }

    public void deletePrice(Long productId, Long priceId) {
        if (priceRepository.deleteByIdAndProductId(priceId, productId) == 0) {
            throw new ResourceNotFoundException("Precio no encontrado para el producto especificado");
        }
    }

}
