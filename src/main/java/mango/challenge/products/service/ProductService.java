package mango.challenge.products.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mango.challenge.products.dto.ProductRequest;
import mango.challenge.products.dto.ProductResponse;
import mango.challenge.products.exception.ResourceNotFoundException;
import mango.challenge.products.model.Product;
import mango.challenge.products.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest productRequest) {
        Product savedProduct = productRepository.save(new Product(productRequest));
        return new ProductResponse(savedProduct);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    public ProductResponse getProductResponseById(Long id) {
        return new ProductResponse(getProductById(id));
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::new)
                .toList();
    }
}