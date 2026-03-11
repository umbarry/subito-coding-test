package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.dto.ProductRequest;
import com.subito.subitocodingtest.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface ProductService {
    @Transactional
    ProductResponse createProduct(ProductRequest request);

    @Transactional(readOnly = true)
    ProductResponse getProduct(Long productId);

    @Transactional(readOnly = true)
    Page<ProductResponse> getAllProducts(Pageable pageable);

    @Transactional
    ProductResponse updateProduct(Long productId, ProductRequest request);

    @Transactional
    void deleteProduct(Long productId);
}
