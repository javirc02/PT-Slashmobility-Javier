package mango.challenge.products.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mango.challenge.products.dto.ProductDTO;
import mango.challenge.products.model.Product;
import mango.challenge.products.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .build();

        Product savedProduct = productRepository.save(product);

        return ProductDTO.builder()
                .id(savedProduct.getId())
                .name(savedProduct.getName())
                .description(savedProduct.getDescription())
                .build();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    public ProductDTO getProductDtoById(Long id) {
        Product product = getProductById(id);

        //Podríamos rellenar los precios del producto llamando a getPrices(id) del PricesService si el sistema lo necesitase
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .build();
    }

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(p -> ProductDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .build())
                .toList();
    }
}