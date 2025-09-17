package mango.challenge.products.service;

import mango.challenge.products.dto.ProductRequest;
import mango.challenge.products.dto.ProductResponse;
import mango.challenge.products.exception.ResourceNotFoundException;
import mango.challenge.products.model.Product;
import mango.challenge.products.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class ProductServiceTest {
    private ProductRepository productRepository;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        productService = new ProductService(productRepository);
    }

    @Test
    void createProduct_shouldSaveAndReturnProductDTO() {
        ProductRequest reques = ProductRequest.builder()
                .name("Zapatillas deportivas")
                .description("Modelo edición limitada")
                .build();

        Product savedProduct = Product.builder()
                .id(1L)
                .name("Zapatillas deportivas")
                .description("Modelo edición limitada")
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(reques);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo(reques.getName());
        assertThat(response.getDescription()).isEqualTo(reques.getDescription());

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getName()).isEqualTo("Zapatillas deportivas");
    }

    @Test
    void getProductById_shouldReturnProduct_whenExists() {
        Product product = Product.builder()
                .id(10L)
                .name("Camiseta")
                .description("Modelo algodon")
                .build();

        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(10L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Camiseta");
    }

    @Test
    void getProductById_shouldThrowException_whenNotExists() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Producto no encontrado");
    }

    @Test
    void getProductDtoById_shouldReturnMappedDTO() {
        Product product = Product.builder()
                .id(5L)
                .name("Pantalón")
                .description("Vaquero slim fit")
                .build();

        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductResponseById(5L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getName()).isEqualTo("Pantalón");
    }

    @Test
    void getAllProducts_shouldReturnListOfDTOs() {
        List<Product> products = List.of(
                Product.builder().id(1L).name("Producto A").description("Desc A").build(),
                Product.builder().id(2L).name("Producto B").description("Desc B").build()
        );

        when(productRepository.findAll()).thenReturn(products);

        List<ProductResponse> response = productService.getAllProducts();

        assertThat(response).hasSize(2);
        assertThat(response.get(0).getName()).isEqualTo("Producto A");
        assertThat(response.get(1).getName()).isEqualTo("Producto B");
    }
}
