package com.subito.subitocodingtest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subito.subitocodingtest.dto.BasketItemRequest;
import com.subito.subitocodingtest.model.*;
import com.subito.subitocodingtest.repository.BasketRepository;
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
import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class BasketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Product product;
    private final String userId = "test-user";

    @BeforeEach
    void setUp() {
        basketRepository.deleteAll();
        productRepository.deleteAll();

        product = Product.builder()
                .name("Test Product")
                .netPrice(new BigDecimal("10.00"))
                .vatPercentage(new BigDecimal("22.00"))
                .availableItems(10)
                .build();
        productRepository.save(product);
    }

    @Test
    void createBasket_shouldCreateBasketSuccessfully() throws Exception {
        mockMvc.perform(post("/v1/users/{userId}/baskets", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // The service returns the created basket, and usually it's 200 OK or 201 Created. Controller defaults to 200 unless specified.
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void createBasket_shouldReturnConflict_whenPendingBasketExists() throws Exception {
        Basket existingBasket = Basket.builder().userId(userId).status(BasketStatus.PENDING).build();
        basketRepository.save(existingBasket);

        mockMvc.perform(post("/v1/users/{userId}/baskets", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void createBasket_shouldCreateNewBasket_whenOnlyCompletedBasketExists() throws Exception {
        Basket completedBasket = Basket.builder().userId(userId).status(BasketStatus.COMPLETED).build();
        basketRepository.save(completedBasket);

        mockMvc.perform(post("/v1/users/{userId}/baskets", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    @Test
    void addItemToBasket_shouldAddNewItemToBasket() throws Exception {
        Basket basket = Basket.builder().userId(userId).status(BasketStatus.PENDING).items(new ArrayList<>()).build();
        basketRepository.save(basket);

        BasketItemRequest itemRequest = new BasketItemRequest(product.getId(), 2);

        mockMvc.perform(post("/v1/users/{userId}/baskets/{basketId}/products", userId, basket.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity", is(2)));
    }

    @Test
    void addItemToBasket_shouldUpdateQuantityForExistingItem() throws Exception {
        Basket basket = Basket.builder().userId(userId).status(BasketStatus.PENDING).items(new ArrayList<>()).build();
        basketRepository.save(basket);

        BasketItem existingItem = BasketItem.builder()
                .basket(basket)
                .product(product)
                .quantity(1)
                .price(BigDecimal.ONE)
                .vat(BigDecimal.TEN)
                .build();
        basket.addItem(existingItem);
        basketRepository.save(basket);

        BasketItemRequest itemRequest = new BasketItemRequest(product.getId(), 2);

        mockMvc.perform(post("/v1/users/{userId}/baskets/{basketId}/products", userId, basket.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity", is(3)));
    }

    @Test
    void addItemToBasket_shouldReturnBadRequest_whenStockIsInsufficient() throws Exception {
        Basket basket = Basket.builder().userId(userId).status(BasketStatus.PENDING).items(new ArrayList<>()).build();
        basketRepository.save(basket);

        BasketItemRequest itemRequest = new BasketItemRequest(product.getId(), 11);

        mockMvc.perform(post("/v1/users/{userId}/baskets/{basketId}/products", userId, basket.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isBadRequest()); 
    }

    @Test
    void addItemToBasket_shouldReturnConflict_whenBasketIsNotPending() throws Exception {
        Basket basket = Basket.builder().userId(userId).status(BasketStatus.COMPLETED).build();
        basketRepository.save(basket);

        BasketItemRequest itemRequest = new BasketItemRequest(product.getId(), 1);

        mockMvc.perform(post("/v1/users/{userId}/baskets/{basketId}/products", userId, basket.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequest)))
                .andExpect(status().isConflict()); 
    }

    @Test
    void removeItemFromBasket_shouldDecreaseQuantity_whenQuantityIsGreaterThanOne() throws Exception {
        Basket basket = Basket.builder().userId(userId).status(BasketStatus.PENDING).items(new ArrayList<>()).build();
        BasketItem item = BasketItem.builder()
                .basket(basket)
                .product(product)
                .quantity(3)
                .price(BigDecimal.ONE)
                .vat(BigDecimal.TEN)
                .build();
        basket.addItem(item);
        basketRepository.save(basket);

        mockMvc.perform(delete("/v1/users/{userId}/baskets/{basketId}/products/{productId}", userId, basket.getId(), product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity", is(2)));
    }

    @Test
    void removeItemFromBasket_shouldRemoveItem_whenQuantityIsOne() throws Exception {
        Basket basket = Basket.builder().userId(userId).status(BasketStatus.PENDING).items(new ArrayList<>()).build();
        BasketItem item = BasketItem.builder()
                .basket(basket)
                .product(product)
                .quantity(1)
                .price(BigDecimal.ONE)
                .vat(BigDecimal.TEN)
                .build();
        basket.addItem(item);
        basketRepository.save(basket);

        mockMvc.perform(delete("/v1/users/{userId}/baskets/{basketId}/products/{productId}", userId, basket.getId(), product.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }
}
