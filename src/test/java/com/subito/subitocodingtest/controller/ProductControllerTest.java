package com.subito.subitocodingtest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subito.subitocodingtest.dto.ProductRequest;
import com.subito.subitocodingtest.model.Product;
import com.subito.subitocodingtest.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void createProduct_shouldReturnCreatedProduct() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .name("Product A")
                .netPrice(BigDecimal.valueOf(10.0))
                .availableItems(5)
                .vatPercentage(BigDecimal.valueOf(22.0))
                .build();

        mockMvc.perform(post("/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))  // objectMapper autowired
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Product A"));
    }

    @Test
    void getProduct_shouldReturnProduct() throws Exception {
        Product product = Product.builder()
                .name("Product A")
                .netPrice(BigDecimal.valueOf(10.0))
                .availableItems(100)
                .vatPercentage(BigDecimal.valueOf(22.0))
                .build();
        product = productRepository.save(product);

        mockMvc.perform(get("/v1/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(product.getId()))
                .andExpect(jsonPath("$.name").value("Product A"));
    }

    @Test
    void getProduct_notFound_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/v1/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllProducts_shouldReturnPagedProducts() throws Exception {
        Product p1 = productRepository.save(Product.builder()
                .name("Product 1")
                .netPrice(BigDecimal.valueOf(10.0))
                .availableItems(5)
                .vatPercentage(BigDecimal.valueOf(22.0))
                .build());
        Product p2 = productRepository.save(Product.builder()
                .name("Product 2")
                .netPrice(BigDecimal.valueOf(20.0))
                .availableItems(10)
                .vatPercentage(BigDecimal.valueOf(22.0))
                .build());

        mockMvc.perform(get("/v1/products?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Product 1"))
                .andExpect(jsonPath("$.content[1].name").value("Product 2"));
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Old Name")
                .netPrice(BigDecimal.valueOf(10.0))
                .availableItems(5)
                .vatPercentage(BigDecimal.valueOf(22.0))
                .build());

        ProductRequest request = ProductRequest.builder()
                .name("Updated Name")
                .netPrice(BigDecimal.valueOf(15.0))
                .availableItems(10)
                .vatPercentage(BigDecimal.valueOf(22.0))
                .build();

        mockMvc.perform(put("/v1/products/" + product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void deleteProduct_shouldReturnNoContent() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("To Delete")
                .netPrice(BigDecimal.valueOf(10.0))
                .availableItems(5)
                .vatPercentage(BigDecimal.valueOf(22.0))
                .build());

        mockMvc.perform(delete("/v1/products/" + product.getId()))
                .andExpect(status().isOk());

        // Verify deleted
        mockMvc.perform(get("/v1/products/" + product.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createProduct_validationFails_returnsBadRequest() throws Exception {
        ProductRequest invalidRequest = ProductRequest.builder()
                .name("")  // invalid
                .netPrice(BigDecimal.valueOf(-1))  // invalid
                .build();

        mockMvc.perform(post("/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
