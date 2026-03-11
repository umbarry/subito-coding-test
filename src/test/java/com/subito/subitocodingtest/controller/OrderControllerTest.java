package com.subito.subitocodingtest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subito.subitocodingtest.dto.CreateOrderRequest;
import com.subito.subitocodingtest.dto.ShipOrderRequest;
import com.subito.subitocodingtest.model.*;
import com.subito.subitocodingtest.repository.BasketRepository;
import com.subito.subitocodingtest.repository.OrderRepository;
import com.subito.subitocodingtest.repository.ProductRepository;
import com.subito.subitocodingtest.service.KafkaProducerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KafkaProducerServiceImpl kafkaProducerService;


    private Product product;
    private Basket basket;

    @BeforeEach
    void setUp() {
        basketRepository.deleteAll();
        orderRepository.deleteAll();
        productRepository.deleteAll();

        product = Product.builder()
                .name("Test Product")
                .netPrice(BigDecimal.TEN)
                .vatPercentage(BigDecimal.TEN)
                .availableItems(10)
                .build();
        productRepository.save(product);

        basket = Basket.builder().status(BasketStatus.PENDING).items(new ArrayList<>()).build();
        basket.addItem(BasketItem.builder()
                .basket(basket)
                .product(product)
                .quantity(1)
                .price(BigDecimal.TEN)
                .vat(BigDecimal.ONE)
                .build());
        basket.setUserId("user-uuid");
        basketRepository.save(basket);
    }

    @Test
    void createOrder_shouldCreateOrderSuccessfully() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(basket.getId(), "Test", "User", "test@example.com", "1234567890", "Test Street", "Test City", "12345", "Test Country");

        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("INSERTED")));
    }

    @Test
    void createOrder_shouldReturnBadRequest_whenBasketIsEmpty() throws Exception {
        basket.getItems().clear();
        basketRepository.save(basket);

        CreateOrderRequest request = new CreateOrderRequest(basket.getId(), "Test", "User", "test@example.com", "1234567890", "Test Street", "Test City", "12345", "Test Country");

        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shipOrder_shouldUpdateOrderStatusAndSendNotification() throws Exception {
        Order order = Order.builder().status(OrderStatus.PAID)
                .email("user@example.com")
                .name("Test Order")
                .lastName("User")
                .street("Test Street")
                .city("Test City")
                .postalCode("12345")
                .country("Test Country")
                .phoneNumber("1234567890")
                .items(new ArrayList<>()).build();
        orderRepository.save(order);

        ShipOrderRequest request = new ShipOrderRequest("http://tracking.url");

        mockMvc.perform(post("/v1/orders/{orderId}/ship", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SHIPPED")));

        verify(kafkaProducerService).sendOrderShipped(argThat(orderShippedEvent -> Objects.equals(orderShippedEvent.getOrderId(), order.getId())));
    }

    @Test
    void shipOrder_shouldReturnConflict_whenOrderIsNotPaid() throws Exception {
        Order order = Order.builder().status(OrderStatus.INSERTED)
                .email("user@example.com")
                .name("Test Order")
                .lastName("User")
                .street("Test Street")
                .city("Test City")
                .postalCode("12345")
                .country("Test Country")
                .phoneNumber("1234567890")
                .items(new ArrayList<>()).build();
        orderRepository.save(order);

        ShipOrderRequest request = new ShipOrderRequest("http://tracking.url");

        mockMvc.perform(post("/v1/orders/{orderId}/ship", order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
