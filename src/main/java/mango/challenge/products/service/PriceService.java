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
        Product product;
        try {
            product = productService.getProductById(productId);
        }catch (Exception e){
            throw new ResourceNotFoundException("Producto no encontrado");
        }

        // Validación de fechas
        if (priceRequest.getEndDate() != null && priceRequest.getInitDate().isAfter(priceRequest.getEndDate())) {
            throw new IllegalArgumentException("initDate debe ser menor que endDate");
        }

        // Validación de solapamiento de fechas
        boolean overlap = product.getPrices().stream().anyMatch(p ->
                (priceRequest.getEndDate() == null || p.getEndDate() == null
                        ? priceRequest.getInitDate().isBefore(p.getEndDate() != null ? p.getEndDate().plusDays(1) : LocalDate.MAX)
                        : !(priceRequest.getEndDate().isBefore(p.getInitDate()) || priceRequest.getInitDate().isAfter(p.getEndDate())))
        );

        if (overlap) {
            throw new IllegalArgumentException("El rango de fechas se solapa con otro precio existente");
        }

        Price savedPrice = priceRepository.save(new Price(priceRequest, product));

        return new PriceResponse(savedPrice);
    }

    public List<PriceResponse> getPrices(Long productId) {
        try {
            productService.getProductById(productId);
        }catch (Exception e){
            throw new ResourceNotFoundException("Producto no encontrado");
        }

        List<Price> prices = priceRepository.findByProductId(productId);
        return prices.stream()
                .map(PriceResponse::new)
                .collect(Collectors.toList());
    }

    public PriceResponse getPriceAtDate(Long productId, LocalDate date) {
        try {
            productService.getProductById(productId);
        }catch (Exception e){
            throw new ResourceNotFoundException("Producto no encontrado");
        }

        List<Price> prices = priceRepository.findByProductId(productId);

        return prices.stream()
                .filter(p -> (p.getInitDate().isEqual(date) || p.getInitDate().isBefore(date)) &&
                        (p.getEndDate() == null || p.getEndDate().isAfter(date) || p.getEndDate().isEqual(date)))
                .findFirst()
                .map(PriceResponse::new)
                .orElseThrow(() -> new IllegalArgumentException("No hay precio vigente para esta fecha"));
    }

    public PriceResponse updatePrice(Long productId, Long priceId, PriceRequest priceRequest) {
        Product product;
        try {
            product = productService.getProductById(productId);
        }catch (Exception e){
            throw new ResourceNotFoundException("Producto no encontrado");
        }

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

        Price updatedPrice = priceRepository.save(existingPrice);

        return new PriceResponse(updatedPrice);
    }

    public void deletePrice(Long productId, Long priceId) {
        int deletedCount = priceRepository.deleteByIdAndProductId(priceId, productId);
        if (deletedCount == 0) {
            throw new ResourceNotFoundException("Precio no encontrado para el producto especificado");
        }
    }

}
