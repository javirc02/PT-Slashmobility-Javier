package mango.challenge.products.service;

import mango.challenge.products.dto.PriceDTO;
import mango.challenge.products.model.Price;
import mango.challenge.products.model.Product;
import mango.challenge.products.repository.PriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class PriceServiceTest {
    private PriceRepository priceRepository;
    private ProductService productService;
    private PriceService priceService;

    @BeforeEach
    void setUp() {
        priceRepository = mock(PriceRepository.class);
        productService = mock(ProductService.class);
        priceService = new PriceService(priceRepository, productService);
    }

    @Test
    void addPrice_shouldSaveAndReturnPriceDTO() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        when(productService.getProductById(1L)).thenReturn(product);

        PriceDTO dto = PriceDTO.builder()
                .value(BigDecimal.valueOf(99.99))
                .initDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 9, 30))
                .build();

        Price savedPrice = Price.builder()
                .id(1L)
                .product(product)
                .value(dto.getValue())
                .initDate(dto.getInitDate())
                .endDate(dto.getEndDate())
                .build();

        when(priceRepository.save(any(Price.class))).thenReturn(savedPrice);

        PriceDTO result = priceService.addPrice(1L, dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getValue()).isEqualTo(BigDecimal.valueOf(99.99));

        ArgumentCaptor<Price> captor = ArgumentCaptor.forClass(Price.class);
        verify(priceRepository).save(captor.capture());
        assertThat(captor.getValue().getValue()).isEqualTo(BigDecimal.valueOf(99.99));
    }

    @Test
    void addPrice_shouldAllowEndDateNull() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        when(productService.getProductById(1L)).thenReturn(product);

        PriceDTO dto = PriceDTO.builder()
                .value(BigDecimal.valueOf(120.0))
                .initDate(LocalDate.of(2025, 10, 1))
                .endDate(null)
                .build();

        Price saved = Price.builder()
                .id(3L)
                .product(product)
                .value(dto.getValue())
                .initDate(dto.getInitDate())
                .endDate(null)
                .build();

        when(priceRepository.save(any(Price.class))).thenReturn(saved);

        PriceDTO result = priceService.addPrice(1L, dto);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getEndDate()).isNull();
    }

    @Test
    void addPrice_shouldAllowConsecutiveDatesWithoutOverlap() {
        Price existing = Price.builder()
                .initDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 9, 10))
                .build();

        Product product = Product.builder().id(1L).prices(List.of(existing)).build();
        when(productService.getProductById(1L)).thenReturn(product);

        PriceDTO dto = PriceDTO.builder()
                .value(BigDecimal.valueOf(75.0))
                .initDate(LocalDate.of(2025, 9, 11)) // empieza justo después del anterior
                .endDate(LocalDate.of(2025, 9, 20))
                .build();

        Price saved = Price.builder().id(5L).value(dto.getValue()).initDate(dto.getInitDate()).endDate(dto.getEndDate()).product(product).build();
        when(priceRepository.save(any(Price.class))).thenReturn(saved);

        PriceDTO result = priceService.addPrice(1L, dto);
        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void addPrice_shouldThrowException_whenInitDateAfterEndDate() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        when(productService.getProductById(1L)).thenReturn(product);

        PriceDTO dto = PriceDTO.builder()
                .value(BigDecimal.valueOf(50.0))
                .initDate(LocalDate.of(2025, 10, 1))
                .endDate(LocalDate.of(2025, 9, 30))
                .build();

        assertThatThrownBy(() -> priceService.addPrice(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("initDate debe ser menor que endDate");
    }

    @Test
    void addPrice_shouldThrowException_whenDateOverlap() {
        Price existing = Price.builder()
                .initDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 9, 10))
                .build();

        Product product = Product.builder().id(1L).prices(List.of(existing)).build();
        when(productService.getProductById(1L)).thenReturn(product);

        PriceDTO dto = PriceDTO.builder()
                .value(BigDecimal.valueOf(60.0))
                .initDate(LocalDate.of(2025, 9, 5))
                .endDate(LocalDate.of(2025, 9, 15))
                .build();

        assertThatThrownBy(() -> priceService.addPrice(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El rango de fechas se solapa con otro precio existente");
    }

    @Test
    void addPrice_shouldThrowException_whenProductNotExists() {
        when(productService.getProductById(99L))
                .thenThrow(new IllegalArgumentException("Producto no encontrado"));

        PriceDTO dto = PriceDTO.builder()
                .value(BigDecimal.valueOf(50.0))
                .initDate(LocalDate.of(2025, 10, 1))
                .endDate(LocalDate.of(2025, 10, 5))
                .build();

        assertThatThrownBy(() -> priceService.addPrice(99L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Producto no encontrado");
    }

    @Test
    void getPriceAtDate_shouldReturnPrice_whenWithinRange() {
        Price p1 = Price.builder()
                .id(1L)
                .value(BigDecimal.valueOf(15.0))
                .initDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 9, 10))
                .build();

        when(priceRepository.findByProductId(1L)).thenReturn(List.of(p1));

        PriceDTO result = priceService.getPriceAtDate(1L, LocalDate.of(2025, 9, 5));

        assertThat(result.getValue()).isEqualTo(BigDecimal.valueOf(15.0));
    }

    @Test
    void getPriceAtDate_shouldReturnPriceWhenEndDateNull() {
        Price p = Price.builder()
                .id(4L)
                .value(BigDecimal.valueOf(200.0))
                .initDate(LocalDate.of(2025, 10, 1))
                .endDate(null)
                .build();

        when(priceRepository.findByProductId(1L)).thenReturn(List.of(p));

        PriceDTO result = priceService.getPriceAtDate(1L, LocalDate.of(2025, 12, 1));
        assertThat(result.getValue()).isEqualTo(BigDecimal.valueOf(200.0));
    }

    @Test
    void getPriceAtDate_shouldThrowException_whenNoPriceFound() {
        when(priceRepository.findByProductId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> priceService.getPriceAtDate(1L, LocalDate.of(2025, 9, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No hay precio vigente para esta fecha");
    }

    @Test
    void getPricesAtDate_shouldThrowException_whenProductNotExists() {
        when(productService.getProductById(99L))
                .thenThrow(new IllegalArgumentException("Producto no encontrado"));

        assertThatThrownBy(() -> priceService.getPriceAtDate(99L, LocalDate.of(2025, 9, 5)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Producto no encontrado");
    }

    @Test
    void getPrices_shouldThrowException_whenProductNotExists() {
        when(productService.getProductById(99L))
                .thenThrow(new IllegalArgumentException("Producto no encontrado"));

        assertThatThrownBy(() -> priceService.getPrices(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Producto no encontrado");
    }

    @Test
    void getPrices_shouldReturnListOfPriceDTOs() {
        Product product = Product.builder().id(1L).build();
        Price price1 = Price.builder().id(1L).value(BigDecimal.valueOf(10.0)).initDate(LocalDate.of(2025, 9, 1)).build();
        Price price2 = Price.builder().id(2L).value(BigDecimal.valueOf(20.0)).initDate(LocalDate.of(2025, 9, 15)).build();

        when(priceRepository.findByProductId(1L)).thenReturn(List.of(price1, price2));

        List<PriceDTO> result = priceService.getPrices(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getValue()).isEqualTo(BigDecimal.valueOf(10.0));
        assertThat(result.get(1).getValue()).isEqualTo(BigDecimal.valueOf(20.0));
    }

    @Test
    void updatePrice_shouldUpdateValueOnly() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        Price existing = Price.builder().id(1L).product(product).value(BigDecimal.valueOf(50.0))
                .initDate(LocalDate.of(2025, 9, 1)).endDate(LocalDate.of(2025, 9, 30)).build();
        product.setPrices(List.of(existing));

        when(productService.getProductById(1L)).thenReturn(product);
        when(priceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(priceRepository.save(any(Price.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PriceDTO dto = PriceDTO.builder().value(BigDecimal.valueOf(60.0)).build();

        PriceDTO updated = priceService.updatePrice(1L, 1L, dto);

        assertThat(updated.getValue()).isEqualTo(BigDecimal.valueOf(60.0));
        assertThat(updated.getInitDate()).isEqualTo(existing.getInitDate());
        assertThat(updated.getEndDate()).isEqualTo(existing.getEndDate());
    }

    @Test
    void updatePrice_shouldUpdateDates() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        Price existing = Price.builder().id(1L).product(product).value(BigDecimal.valueOf(50.0))
                .initDate(LocalDate.of(2025, 9, 1)).endDate(LocalDate.of(2025, 9, 30)).build();
        product.setPrices(List.of(existing));

        when(productService.getProductById(1L)).thenReturn(product);
        when(priceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(priceRepository.save(any(Price.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PriceDTO dto = PriceDTO.builder()
                .initDate(LocalDate.of(2025, 9, 5))
                .endDate(LocalDate.of(2025, 10, 5))
                .build();

        PriceDTO updated = priceService.updatePrice(1L, 1L, dto);

        assertThat(updated.getInitDate()).isEqualTo(LocalDate.of(2025, 9, 5));
        assertThat(updated.getEndDate()).isEqualTo(LocalDate.of(2025, 10, 5));
        assertThat(updated.getValue()).isEqualTo(existing.getValue());
    }

    @Test
    void updatePrice_shouldThrowException_whenInitDateAfterEndDate() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        Price existing = Price.builder().id(1L).product(product)
                .initDate(LocalDate.of(2025, 9, 1)).endDate(LocalDate.of(2025, 9, 30)).build();
        product.setPrices(List.of(existing));

        when(productService.getProductById(1L)).thenReturn(product);
        when(priceRepository.findById(1L)).thenReturn(Optional.of(existing));

        PriceDTO dto = PriceDTO.builder()
                .initDate(LocalDate.of(2025, 10, 1))
                .endDate(LocalDate.of(2025, 9, 30))
                .build();

        assertThatThrownBy(() -> priceService.updatePrice(1L, 1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("initDate debe ser menor que endDate");
    }

    @Test
    void updatePrice_shouldThrowException_whenDateOverlap() {
        Price p1 = Price.builder().id(1L).initDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 9, 10)).build();
        Price p2 = Price.builder().id(2L).initDate(LocalDate.of(2025, 9, 15))
                .endDate(LocalDate.of(2025, 9, 20)).build();
        Product product = Product.builder().id(1L).prices(List.of(p1, p2)).build();

        when(productService.getProductById(1L)).thenReturn(product);
        when(priceRepository.findById(1L)).thenReturn(Optional.of(p1));

        PriceDTO dto = PriceDTO.builder()
                .initDate(LocalDate.of(2025, 9, 8))
                .endDate(LocalDate.of(2025, 9, 18))
                .build();

        assertThatThrownBy(() -> priceService.updatePrice(1L, 1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El rango de fechas se solapa con otro precio existente");
    }

    @Test
    void updatePrice_shouldThrowException_whenProductNotFound() {
        when(productService.getProductById(1L)).thenThrow(new IllegalArgumentException("Producto no encontrado"));

        PriceDTO dto = PriceDTO.builder().value(BigDecimal.valueOf(50)).build();

        assertThatThrownBy(() -> priceService.updatePrice(1L, 1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Producto no encontrado");
    }

    @Test
    void updatePrice_shouldThrowException_whenPriceNotFound() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        when(productService.getProductById(1L)).thenReturn(product);
        when(priceRepository.findById(1L)).thenReturn(Optional.empty());

        PriceDTO dto = PriceDTO.builder().value(BigDecimal.valueOf(50)).build();

        assertThatThrownBy(() -> priceService.updatePrice(1L, 1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Precio no encontrado");
    }

    @Test
    void updatePrice_shouldThrowException_whenPriceDoesNotBelongToProduct() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        Price otherPrice = Price.builder().id(2L).build();
        when(productService.getProductById(1L)).thenReturn(product);
        when(priceRepository.findById(2L)).thenReturn(Optional.of(otherPrice));

        PriceDTO dto = PriceDTO.builder().value(BigDecimal.valueOf(50)).build();

        assertThatThrownBy(() -> priceService.updatePrice(1L, 2L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El precio no pertenece al producto");
    }

    @Test
    void deletePrice_shouldDeleteSuccessfully() {
        Product product = Product.builder().id(1L).prices(new ArrayList<>()).build();
        Price price = Price.builder().id(1L).value(BigDecimal.valueOf(100)).initDate(LocalDate.now()).build();
        product.getPrices().add(price);

        when(productService.getProductById(1L)).thenReturn(product);
        when(priceRepository.findById(1L)).thenReturn(Optional.of(price));

        priceService.deletePrice(1L, 1L);

        verify(priceRepository).delete(price);
        assert(product.getPrices().isEmpty());
    }

    @Test
    void deletePrice_shouldThrow_whenProductNotFound() {
        when(productService.getProductById(1L)).thenThrow(new IllegalArgumentException("Producto no encontrado"));

        assertThatThrownBy(() -> priceService.deletePrice(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Producto no encontrado");
    }

    @Test
    void deletePrice_shouldThrow_whenPriceNotFound() {
        Product product = Product.builder().id(1L).prices(new ArrayList<>()).build();
        when(productService.getProductById(1L)).thenReturn(product);
        when(priceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> priceService.deletePrice(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Precio no encontrado");
    }

    @Test
    void deletePrice_shouldThrow_whenPriceNotBelongToProduct() {
        Product product = Product.builder().id(1L).prices(new ArrayList<>()).build();
        Price price = Price.builder().id(1L).value(BigDecimal.valueOf(100)).initDate(LocalDate.now()).build();
        // No añadimos el precio a la lista de product.getPrices()

        when(productService.getProductById(1L)).thenReturn(product);
        when(priceRepository.findById(1L)).thenReturn(Optional.of(price));

        assertThatThrownBy(() -> priceService.deletePrice(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El precio no pertenece al producto");
    }

}
