package mango.challenge.products.service;

import mango.challenge.products.dto.PriceRequest;
import mango.challenge.products.dto.PriceResponse;
import mango.challenge.products.exception.ResourceNotFoundException;
import mango.challenge.products.model.Price;
import mango.challenge.products.model.Product;
import mango.challenge.products.repository.PriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        when(productService.getProductByIdOrThrow(1L)).thenReturn(product);

        PriceRequest request = PriceRequest.builder()
                .value(BigDecimal.valueOf(99.99))
                .initDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 9, 30))
                .build();

        Price savedPrice = Price.builder()
                .id(1L)
                .product(product)
                .value(request.getValue())
                .initDate(request.getInitDate())
                .endDate(request.getEndDate())
                .build();

        when(priceRepository.save(any(Price.class))).thenReturn(savedPrice);

        PriceResponse result = priceService.addPrice(1L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getValue()).isEqualTo(BigDecimal.valueOf(99.99));

        ArgumentCaptor<Price> captor = ArgumentCaptor.forClass(Price.class);
        verify(priceRepository).save(captor.capture());
        assertThat(captor.getValue().getValue()).isEqualTo(BigDecimal.valueOf(99.99));
    }

    @Test
    void addPrice_shouldAllowEndDateNull() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        when(productService.getProductByIdOrThrow(1L)).thenReturn(product);

        PriceRequest dto = PriceRequest.builder()
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

        PriceResponse response = priceService.addPrice(1L, dto);

        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getEndDate()).isNull();
    }

    @Test
    void addPrice_shouldAllowConsecutiveDatesWithoutOverlap() {
        Price existing = Price.builder()
                .initDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 9, 10))
                .build();

        Product product = Product.builder().id(1L).prices(List.of(existing)).build();
        when(productService.getProductByIdOrThrow(1L)).thenReturn(product);

        PriceRequest dto = PriceRequest.builder()
                .value(BigDecimal.valueOf(75.0))
                .initDate(LocalDate.of(2025, 9, 11)) // empieza justo después del anterior
                .endDate(LocalDate.of(2025, 9, 20))
                .build();

        Price saved = Price.builder().id(5L).value(dto.getValue()).initDate(dto.getInitDate()).endDate(dto.getEndDate()).product(product).build();
        when(priceRepository.save(any(Price.class))).thenReturn(saved);

        PriceResponse response = priceService.addPrice(1L, dto);
        assertThat(response.getId()).isEqualTo(5L);
    }

    @Test
    void addPrice_shouldThrowException_whenInitDateAfterEndDate() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        when(productService.getProductByIdOrThrow(1L)).thenReturn(product);

        PriceRequest dto = PriceRequest.builder()
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
        Product product = Product.builder().id(1L).build();
        when(productService.getProductByIdOrThrow(1L)).thenReturn(product);

        PriceRequest dto = PriceRequest.builder()
                .value(BigDecimal.valueOf(60.0))
                .initDate(LocalDate.of(2025, 9, 5))
                .endDate(LocalDate.of(2025, 9, 15))
                .build();

        when(priceRepository.existsOverlappingPrice(1L, dto.getInitDate(), dto.getEndDate()))
                .thenReturn(true);

        assertThatThrownBy(() -> priceService.addPrice(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El rango de fechas se solapa con otro precio existente");

        verify(priceRepository, never()).save(any());
    }

    @Test
    void addPrice_shouldThrowException_whenProductNotExists() {
        when(productService.getProductByIdOrThrow(99L))
                .thenThrow(new ResourceNotFoundException("Producto no encontrado"));

        PriceRequest dto = PriceRequest.builder()
                .value(BigDecimal.valueOf(50.0))
                .initDate(LocalDate.of(2025, 10, 1))
                .endDate(LocalDate.of(2025, 10, 5))
                .build();

        assertThatThrownBy(() -> priceService.addPrice(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Producto no encontrado");
    }

    @Test
    void getPriceAtDate_shouldReturnPrice_whenWithinRange() {
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Desc")
                .build();

        Price p1 = Price.builder()
                .id(1L)
                .value(BigDecimal.valueOf(15.0))
                .initDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 9, 10))
                .product(product)
                .build();

        when(productService.getProductByIdOrThrow(1L)).thenReturn(product);
        when(priceRepository.findActivePriceAtDate(1L, LocalDate.of(2025, 9, 5)))
                .thenReturn(Optional.of(p1));


        PriceResponse response = priceService.getPriceAtDate(1L, LocalDate.of(2025, 9, 5));

        assertThat(response.getValue()).isEqualTo(BigDecimal.valueOf(15.0));
    }

    @Test
    void getPriceAtDate_shouldReturnPriceWhenEndDateNull() {
        Product product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Desc")
                .build();

        Price p = Price.builder()
                .id(4L)
                .value(BigDecimal.valueOf(200.0))
                .initDate(LocalDate.of(2025, 10, 1))
                .endDate(null)
                .product(product)
                .build();

        when(productService.getProductByIdOrThrow(1L)).thenReturn(product);
        when(priceRepository.findActivePriceAtDate(1L, LocalDate.of(2025, 12, 1)))
                .thenReturn(Optional.of(p));

        PriceResponse response = priceService.getPriceAtDate(1L, LocalDate.of(2025, 12, 1));
        assertThat(response.getValue()).isEqualTo(BigDecimal.valueOf(200.0));
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
        when(productService.getProductByIdOrThrow(99L))
                .thenThrow(new ResourceNotFoundException("Producto no encontrado"));

        assertThatThrownBy(() -> priceService.getPriceAtDate(99L, LocalDate.of(2025, 9, 5)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Producto no encontrado");
    }

    @Test
    void getPrices_shouldThrowException_whenProductNotExists() {
        when(productService.getProductByIdOrThrow(99L))
                .thenThrow(new ResourceNotFoundException("Producto no encontrado"));

        assertThatThrownBy(() -> priceService.getPrices(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Producto no encontrado");
    }

    @Test
    void getPrices_shouldReturnListOfPriceDTOs() {
        Product product1 = Product.builder().id(1L).build();
        Product product2 = Product.builder().id(2L).build();
        Price price1 = Price.builder().id(1L).value(BigDecimal.valueOf(10.0)).initDate(LocalDate.of(2025, 9, 1)).product(product1).build();
        Price price2 = Price.builder().id(2L).value(BigDecimal.valueOf(20.0)).initDate(LocalDate.of(2025, 9, 15)).product(product2).build();

        when(priceRepository.findByProductId(1L)).thenReturn(List.of(price1, price2));

        List<PriceResponse> response = priceService.getPrices(1L);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).getValue()).isEqualTo(BigDecimal.valueOf(10.0));
        assertThat(response.get(1).getValue()).isEqualTo(BigDecimal.valueOf(20.0));
    }

    @Test
    void updatePrice_shouldUpdateValueOnly() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        Price existing = Price.builder().id(1L).product(product).value(BigDecimal.valueOf(50.0))
                .initDate(LocalDate.of(2025, 9, 1)).endDate(LocalDate.of(2025, 9, 30)).build();
        product.setPrices(List.of(existing));

        when(productService.getProductByIdOrThrow(1L)).thenReturn(product);
        when(priceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(priceRepository.save(any(Price.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PriceRequest request = PriceRequest.builder().value(BigDecimal.valueOf(60.0)).build();

        PriceResponse response = priceService.updatePrice(1L, 1L, request);

        assertThat(response.getValue()).isEqualTo(BigDecimal.valueOf(60.0));
        assertThat(response.getInitDate()).isEqualTo(existing.getInitDate());
        assertThat(response.getEndDate()).isEqualTo(existing.getEndDate());
    }

    @Test
    void updatePrice_shouldUpdateDates() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        Price existing = Price.builder().id(1L).product(product).value(BigDecimal.valueOf(50.0))
                .initDate(LocalDate.of(2025, 9, 1)).endDate(LocalDate.of(2025, 9, 30)).build();
        product.setPrices(List.of(existing));

        when(productService.getProductByIdOrThrow(1L)).thenReturn(product);
        when(priceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(priceRepository.save(any(Price.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PriceRequest dto = PriceRequest.builder()
                .initDate(LocalDate.of(2025, 9, 5))
                .endDate(LocalDate.of(2025, 10, 5))
                .build();

        PriceResponse response = priceService.updatePrice(1L, 1L, dto);

        assertThat(response.getInitDate()).isEqualTo(LocalDate.of(2025, 9, 5));
        assertThat(response.getEndDate()).isEqualTo(LocalDate.of(2025, 10, 5));
        assertThat(response.getValue()).isEqualTo(existing.getValue());
    }

    @Test
    void updatePrice_shouldThrowException_whenInitDateAfterEndDate() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        Price existing = Price.builder().id(1L).product(product)
                .initDate(LocalDate.of(2025, 9, 1)).endDate(LocalDate.of(2025, 9, 30)).build();
        product.setPrices(List.of(existing));

        when(productService.getProductByIdOrThrow(1L)).thenReturn(product);
        when(priceRepository.findById(1L)).thenReturn(Optional.of(existing));

        PriceRequest dto = PriceRequest.builder()
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

        when(productService.getProductByIdOrThrow(1L)).thenReturn(product);
        when(priceRepository.findById(1L)).thenReturn(Optional.of(p1));

        PriceRequest dto = PriceRequest.builder()
                .initDate(LocalDate.of(2025, 9, 8))
                .endDate(LocalDate.of(2025, 9, 18))
                .build();

        assertThatThrownBy(() -> priceService.updatePrice(1L, 1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El rango de fechas se solapa con otro precio existente");
    }

    @Test
    void updatePrice_shouldThrowException_whenProductNotFound() {
        when(productService.getProductByIdOrThrow(1L)).thenThrow(new ResourceNotFoundException("Producto no encontrado"));

        PriceRequest dto = PriceRequest.builder().value(BigDecimal.valueOf(50)).build();

        assertThatThrownBy(() -> priceService.updatePrice(1L, 1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Producto no encontrado");
    }

    @Test
    void updatePrice_shouldThrowException_whenPriceNotFound() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        when(productService.getProductByIdOrThrow(1L)).thenReturn(product);
        when(priceRepository.findById(1L)).thenReturn(Optional.empty());

        PriceRequest dto = PriceRequest.builder().value(BigDecimal.valueOf(50)).build();

        assertThatThrownBy(() -> priceService.updatePrice(1L, 1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Precio no encontrado");
    }

    @Test
    void updatePrice_shouldThrowException_whenPriceDoesNotBelongToProduct() {
        Product product = Product.builder().id(1L).prices(List.of()).build();
        Price otherPrice = Price.builder().id(2L).build();
        when(productService.getProductByIdOrThrow(1L)).thenReturn(product);
        when(priceRepository.findById(2L)).thenReturn(Optional.of(otherPrice));

        PriceRequest dto = PriceRequest.builder().value(BigDecimal.valueOf(50)).build();

        assertThatThrownBy(() -> priceService.updatePrice(1L, 2L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("El precio no pertenece al producto");
    }

    @Test
    void deletePrice_shouldDeleteSuccessfully() {
        when(priceRepository.deleteByIdAndProductId(1L, 1L)).thenReturn(1);

        priceService.deletePrice(1L, 1L);

        verify(priceRepository).deleteByIdAndProductId(1L, 1L);
    }

    @Test
    void deletePrice_shouldThrow_whenPriceNotFound() {
        when(priceRepository.deleteByIdAndProductId(1L, 1L)).thenReturn(0);

        assertThatThrownBy(() -> priceService.deletePrice(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Precio no encontrado para el producto especificado");
    }
}
