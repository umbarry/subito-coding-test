package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.dto.ProductRequest;
import com.subito.subitocodingtest.dto.ProductResponse;
import com.subito.subitocodingtest.exception.ResourceNotFoundException;
import com.subito.subitocodingtest.exception.ResourceType;
import com.subito.subitocodingtest.model.Product;
import com.subito.subitocodingtest.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating product: {}", request.getName());
        Product product = Product.builder()
                .name(request.getName())
                .netPrice(request.getNetPrice())
                .vatPercentage(request.getVatPercentage())
                .availableItems(request.getAvailableItems())
                .build();

        Product savedProduct = productRepository.save(product);
        log.debug("Product created with ID: {} - Name: {}, Available items: {}", savedProduct.getId(), savedProduct.getName(), savedProduct.getAvailableItems());
        return ProductResponse.fromProduct(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long productId) {
        log.info("Retrieving product with ID: {}", productId);
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            log.warn("Product not found with ID: {}", productId);
            throw new ResourceNotFoundException(ResourceType.PRODUCT, productId);
        }
        log.debug("Product found: {}", product.get().getName());
        return ProductResponse.fromProduct(product.get());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        log.info("Retrieving all products - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<ProductResponse> products = productRepository.findAll(pageable)
                .map(ProductResponse::fromProduct);
        log.debug("Retrieved {} products", products.getNumberOfElements());
        return products;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest request) {
        log.info("Updating product with ID: {}", productId);
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            log.warn("Product not found with ID: {}", productId);
            throw new ResourceNotFoundException(ResourceType.PRODUCT, productId);
        }

        Product product = productOpt.get();
        product.setName(request.getName());
        product.setNetPrice(request.getNetPrice());
        product.setVatPercentage(request.getVatPercentage());
        product.setAvailableItems(request.getAvailableItems());

        Product updatedProduct = productRepository.save(product);
        log.debug("Product updated - ID: {}, Name: {}", updatedProduct.getId(), updatedProduct.getName());
        return ProductResponse.fromProduct(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        log.info("Deleting product with ID: {}", productId);
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            log.warn("Product not found with ID: {}", productId);
            throw new ResourceNotFoundException(ResourceType.PRODUCT, productId);
        }
        productRepository.deleteById(productId);
        log.debug("Product deleted successfully");
    }
}
