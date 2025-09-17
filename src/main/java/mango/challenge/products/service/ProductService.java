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
        return new ProductResponse(productRepository.save(new Product(productRequest)));
    }

    public ProductResponse getProductById(Long id) {
        return new ProductResponse(getProductByIdOrThrow(id));
    }

    public Product getProductByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::new)
                .toList();
    }
}