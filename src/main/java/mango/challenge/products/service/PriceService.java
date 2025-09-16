package mango.challenge.products.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mango.challenge.products.dto.PriceDTO;
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

    public PriceDTO addPrice(Long productId, PriceDTO priceDTO) {
        Product product = productService.getProductById(productId);

        // Validación de fechas
        if (priceDTO.getEndDate() != null && priceDTO.getInitDate().isAfter(priceDTO.getEndDate())) {
            throw new IllegalArgumentException("initDate debe ser menor que endDate");
        }

        // Validación de solapamiento de fechas
        boolean overlap = product.getPrices().stream().anyMatch(p ->
                (priceDTO.getEndDate() == null || p.getEndDate() == null
                        ? priceDTO.getInitDate().isBefore(p.getEndDate() != null ? p.getEndDate().plusDays(1) : LocalDate.MAX)
                        : !(priceDTO.getEndDate().isBefore(p.getInitDate()) || priceDTO.getInitDate().isAfter(p.getEndDate())))
        );

        if (overlap) {
            throw new IllegalArgumentException("El rango de fechas se solapa con otro precio existente");
        }

        Price price = Price.builder()
                .product(product)
                .value(priceDTO.getValue())
                .initDate(priceDTO.getInitDate())
                .endDate(priceDTO.getEndDate())
                .build();

        Price savedPrice = priceRepository.save(price);

        return PriceDTO.builder()
                .id(savedPrice.getId())
                .value(savedPrice.getValue())
                .initDate(savedPrice.getInitDate())
                .endDate(savedPrice.getEndDate())
                .build();
    }

    public List<PriceDTO> getPrices(Long productId) {
        try {
            Product product = productService.getProductById(productId);
        }catch (Exception e){
            throw new IllegalArgumentException("Producto no encontrado");
        }

        List<Price> prices = priceRepository.findByProductId(productId);
        return prices.stream()
                .map(p -> PriceDTO.builder()
                        .id(p.getId())
                        .value(p.getValue())
                        .initDate(p.getInitDate())
                        .endDate(p.getEndDate())
                        .build())
                .collect(Collectors.toList());
    }

    public PriceDTO getPriceAtDate(Long productId, LocalDate date) {
        try {
            Product product = productService.getProductById(productId);
        }catch (Exception e){
            throw new IllegalArgumentException("Producto no encontrado");
        }

        List<Price> prices = priceRepository.findByProductId(productId);

        return prices.stream()
                .filter(p -> (p.getInitDate().isEqual(date) || p.getInitDate().isBefore(date)) &&
                        (p.getEndDate() == null || p.getEndDate().isAfter(date) || p.getEndDate().isEqual(date)))
                .findFirst()
                .map(p -> PriceDTO.builder()
                        .id(p.getId())
                        .value(p.getValue())
                        .initDate(p.getInitDate())
                        .endDate(p.getEndDate())
                        .build())
                .orElseThrow(() -> new IllegalArgumentException("No hay precio vigente para esta fecha"));
    }

    public PriceDTO updatePrice(Long productId, Long priceId, PriceDTO priceDTO) {
        Product product;
        try {
            product = productService.getProductById(productId);
        }catch (Exception e){
            throw new IllegalArgumentException("Producto no encontrado");
        }

        Price existingPrice = priceRepository.findById(priceId)
                .orElseThrow(() -> new IllegalArgumentException("Precio no encontrado"));

        if (!product.getPrices().contains(existingPrice)) {
            throw new IllegalArgumentException("El precio no pertenece al producto");
        }

        if (priceDTO.getValue() != null) {
            existingPrice.setValue(priceDTO.getValue());
        }
        if (priceDTO.getInitDate() != null) {
            existingPrice.setInitDate(priceDTO.getInitDate());
        }
        if (priceDTO.getEndDate() != null) {
            existingPrice.setEndDate(priceDTO.getEndDate());
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

        return PriceDTO.builder()
                .id(updatedPrice.getId())
                .value(updatedPrice.getValue())
                .initDate(updatedPrice.getInitDate())
                .endDate(updatedPrice.getEndDate())
                .build();
    }

    public void deletePrice(Long productId, Long priceId) {
        Product product;
        try {
            product = productService.getProductById(productId);
        }catch (Exception e){
            throw new IllegalArgumentException("Producto no encontrado");
        }

        Price existingPrice = priceRepository.findById(priceId)
                .orElseThrow(() -> new IllegalArgumentException("Precio no encontrado"));

        if (!product.getPrices().contains(existingPrice)) {
            throw new IllegalArgumentException("El precio no pertenece al producto");
        }

        product.getPrices().remove(existingPrice);
        priceRepository.delete(existingPrice);
    }

}
