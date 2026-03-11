package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.dto.CreateOrderRequest;
import com.subito.subitocodingtest.dto.OrderResponse;
import com.subito.subitocodingtest.dto.ShipOrderRequest;
import com.subito.subitocodingtest.events.OrderExpirationEvent;
import com.subito.subitocodingtest.events.OrderShippedEvent;
import com.subito.subitocodingtest.model.*;
import com.subito.subitocodingtest.repository.BasketRepository;
import com.subito.subitocodingtest.repository.OrderRepository;
import com.subito.subitocodingtest.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BasketRepository basketRepository;

    @Mock
    private KafkaProducerServiceImpl kafkaProducerService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Product product;
    private Basket basket;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .netPrice(BigDecimal.TEN)
                .vatPercentage(BigDecimal.TEN)
                .availableItems(10)
                .build();

        basket = Basket.builder().id(1L).status(BasketStatus.PENDING).items(new ArrayList<>()).build();
        basket.addItem(BasketItem.builder().product(product).quantity(1).build());
    }

    @Test
    void createOrder_shouldCreateOrderSuccessfully() {
        CreateOrderRequest request = new CreateOrderRequest(1L, "Test", "User", "test@example.com", "1234567890", "Test Street", "Test City", "12345", "Test Country");

        when(basketRepository.findById(1L)).thenReturn(Optional.of(basket));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse orderResponse = orderService.createOrder(request);

        assertEquals(OrderStatus.INSERTED, orderResponse.getStatus());
        assertEquals(9, product.getAvailableItems());
        assertEquals(BasketStatus.COMPLETED, basket.getStatus());
    }

    @Test
    void createOrder_shouldThrowIllegalArgumentException_whenBasketIsEmpty() {
        basket.getItems().clear();
        CreateOrderRequest request = new CreateOrderRequest(1L, "Test", "User", "test@example.com", "1234567890", "Test Street", "Test City", "12345", "Test Country");

        when(basketRepository.findById(1L)).thenReturn(Optional.of(basket));

        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));
    }

    @Test
    void expireUnpaidOrders_shouldExpireOldOrdersAndRestoreStock() {
        Order oldOrder = Order.builder().id(1L).status(OrderStatus.INSERTED).items(new ArrayList<>()).createdAt(LocalDateTime.now().minusHours(2)).build();
        oldOrder.addItem(OrderItem.builder().product(product).quantity(2).build());

        when(orderRepository.findByStatusAndCreatedAtBefore(any(), any())).thenReturn(List.of(oldOrder));

        orderService.expireUnpaidOrders();

        assertEquals(OrderStatus.EXPIRED, oldOrder.getStatus());
        assertEquals(12, product.getAvailableItems());
        verify(kafkaProducerService).sendOrderExpiration(any(OrderExpirationEvent.class));
    }

    @Test
    void shipOrder_shouldUpdateOrderStatusAndSendNotification() {
        Order order = Order.builder().id(1L).status(OrderStatus.PAID).items(new ArrayList<>()).build();
        ShipOrderRequest request = new ShipOrderRequest("http://tracking.url");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.shipOrder(1L, request);

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertEquals("http://tracking.url", order.getTrackingUrl());
        verify(kafkaProducerService).sendOrderShipped(any(OrderShippedEvent.class));
    }

    @Test
    void shipOrder_shouldThrowIllegalStateException_whenOrderIsNotPaid() {
        Order order = Order.builder().id(1L).status(OrderStatus.INSERTED).build();
        ShipOrderRequest request = new ShipOrderRequest("http://tracking.url");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.shipOrder(1L, request));
    }
}
