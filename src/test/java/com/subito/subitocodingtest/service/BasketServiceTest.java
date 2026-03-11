package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.dto.BasketItemRequest;
import com.subito.subitocodingtest.dto.BasketResponse;
import com.subito.subitocodingtest.exception.BasketAlreadyExistsException;
import com.subito.subitocodingtest.exception.BasketStatusException;
import com.subito.subitocodingtest.model.*;
import com.subito.subitocodingtest.repository.BasketRepository;
import com.subito.subitocodingtest.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasketServiceTest {

    @Mock
    private BasketRepository basketRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private BasketService basketService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .netPrice(new BigDecimal("10.00"))
                .vatPercentage(new BigDecimal("22.00"))
                .availableItems(10)
                .build();
    }

    @Test
    void createBasket_shouldCreateBasketSuccessfully() {
        String userId = "test-user";
        Basket basket = Basket.builder().id(1L).userId(userId).status(BasketStatus.PENDING).build();
        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.PENDING)).thenReturn(Collections.emptyList());
        when(basketRepository.save(any(Basket.class))).thenReturn(basket);

        BasketResponse basketResponse = basketService.createBasket(userId);

        assertEquals(basket.getId(), basketResponse.getId());
    }

    @Test
    void createBasket_shouldThrowBasketAlreadyExistsException_whenPendingBasketExists() {
        String userId = "test-user";
        Basket existingBasket = Basket.builder().id(1L).userId(userId).status(BasketStatus.PENDING).build();
        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.PENDING)).thenReturn(List.of(existingBasket));

        assertThrows(BasketAlreadyExistsException.class, () -> basketService.createBasket(userId));
    }

    @Test
    void createBasket_shouldCreateNewBasket_whenOnlyCompletedBasketExists() {
        String userId = "test-user";
        Basket newBasket = Basket.builder().id(2L).userId(userId).status(BasketStatus.PENDING).build();
        when(basketRepository.findByUserIdAndStatus(userId, BasketStatus.PENDING)).thenReturn(Collections.emptyList());
        when(basketRepository.save(any(Basket.class))).thenReturn(newBasket);

        BasketResponse basketResponse = basketService.createBasket(userId);

        assertNotNull(basketResponse);
        assertEquals(2L, basketResponse.getId());
    }

    @Test
    void addItemToBasket_shouldAddNewItemToBasket() {
        Basket basket = Basket.builder().id(1L).status(BasketStatus.PENDING).items(new ArrayList<>()).build();
        BasketItemRequest itemRequest = new BasketItemRequest(1L, 2);

        when(basketRepository.findById(1L)).thenReturn(Optional.of(basket));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(basketRepository.save(any(Basket.class))).thenReturn(basket);

        basketService.addItemToBasket(1L, itemRequest);

        assertEquals(1, basket.getItems().size());
        assertEquals(2, basket.getItems().get(0).getQuantity());
    }

    @Test
    void addItemToBasket_shouldUpdateQuantityForExistingItem() {
        BasketItem existingItem = BasketItem.builder().product(product).quantity(1).price(BigDecimal.ONE).vat(BigDecimal.TEN).build();
        Basket basket = Basket.builder().id(1L).status(BasketStatus.PENDING).items(new ArrayList<>()).build();
        basket.addItem(existingItem);
        BasketItemRequest itemRequest = new BasketItemRequest(1L, 2);

        when(basketRepository.findById(1L)).thenReturn(Optional.of(basket));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(basketRepository.save(any(Basket.class))).thenReturn(basket);

        basketService.addItemToBasket(1L, itemRequest);

        assertEquals(1, basket.getItems().size());
        assertEquals(3, basket.getItems().get(0).getQuantity());
    }

    @Test
    void addItemToBasket_shouldThrowIllegalArgumentException_whenStockIsInsufficient() {
        Basket basket = Basket.builder().id(1L).status(BasketStatus.PENDING).items(new ArrayList<>()).build();
        BasketItemRequest itemRequest = new BasketItemRequest(1L, 11);

        when(basketRepository.findById(1L)).thenReturn(Optional.of(basket));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> basketService.addItemToBasket(1L, itemRequest));
    }

    @Test
    void addItemToBasket_shouldThrowBasketStatusException_whenBasketIsNotPending() {
        Basket basket = Basket.builder().id(1L).status(BasketStatus.COMPLETED).build();
        BasketItemRequest itemRequest = new BasketItemRequest(1L, 1);

        when(basketRepository.findById(1L)).thenReturn(Optional.of(basket));

        assertThrows(BasketStatusException.class, () -> basketService.addItemToBasket(1L, itemRequest));
    }

    @Test
    void removeItemFromBasket_shouldDecreaseQuantity_whenQuantityIsGreaterThanOne() {
        BasketItem item = BasketItem.builder().product(product).quantity(3).price(BigDecimal.ONE).vat(BigDecimal.TEN).build();
        Basket basket = Basket.builder().id(1L).status(BasketStatus.PENDING).items(new ArrayList<>()).build();
        basket.addItem(item);

        when(basketRepository.findById(1L)).thenReturn(Optional.of(basket));
        when(basketRepository.save(any(Basket.class))).thenReturn(basket);

        basketService.removeItemFromBasket(1L, 1L);

        assertEquals(2, item.getQuantity());
    }

    @Test
    void removeItemFromBasket_shouldRemoveItem_whenQuantityIsOne() {
        BasketItem item = BasketItem.builder().product(product).quantity(1).build();
        Basket basket = Basket.builder().id(1L).status(BasketStatus.PENDING).items(new ArrayList<>()).build();
        basket.addItem(item);

        when(basketRepository.findById(1L)).thenReturn(Optional.of(basket));
        when(basketRepository.save(any(Basket.class))).thenReturn(basket);

        basketService.removeItemFromBasket(1L, 1L);

        assertTrue(basket.getItems().isEmpty());
    }
}
