package com.subito.subitocodingtest.controller;

import com.subito.subitocodingtest.model.Order;
import com.subito.subitocodingtest.model.OrderStatus;
import com.subito.subitocodingtest.repository.OrderRepository;
import com.subito.subitocodingtest.repository.PaymentRepository;
import com.subito.subitocodingtest.service.JwtService;
import com.subito.subitocodingtest.service.KafkaProducerServiceImpl;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private KafkaProducerServiceImpl kafkaProducerService;

    private Order order;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        orderRepository.deleteAll();

        order = Order.builder().status(OrderStatus.INSERTED)
                .email("user@example.com")
                .name("Test Order")
                .lastName("User")
                .street("Test Street")
                .city("Test City")
                .postalCode("12345")
                .country("Test Country")
                .phoneNumber("1234567890")
                .build();
        orderRepository.save(order);
    }

    @Test
    void paymentWebhook_shouldUpdateOrderStatusToPaidWhenPaymentIsAccepted() throws Exception {
        String token = "test-token";
        Claims claims = mock(Claims.class);
        when(claims.get(eq("orderId"), eq(Long.class))).thenReturn(order.getId());
        when(claims.get(eq("paymentId"), eq(String.class))).thenReturn("pay_123");
        when(claims.get(eq("paymentAccepted"), eq(Boolean.class))).thenReturn(true);

        when(jwtService.parseToken(token)).thenReturn(claims);

        mockMvc.perform(get("/v1/payments/webhook").param("token", token))
                .andExpect(status().isOk());

        verify(kafkaProducerService).sendPaymentConfirmation(argThat(paymentConfirmationEvent -> Objects.equals(paymentConfirmationEvent.getOrderId(), order.getId())));
    }

    @Test
    void paymentWebhook_shouldNotUpdateOrderStatus_whenPaymentIsRejected() throws Exception {
        String token = "test-token";
        Claims claims = mock(Claims.class);
        when(claims.get(eq("orderId"), eq(Long.class))).thenReturn(order.getId());
        when(claims.get(eq("paymentId"), eq(String.class))).thenReturn("pay_123");
        when(claims.get(eq("paymentAccepted"), eq(Boolean.class))).thenReturn(false);

        when(jwtService.parseToken(token)).thenReturn(claims);

        mockMvc.perform(get("/v1/payments/webhook").param("token", token))
                .andExpect(status().isOk());

        verify(kafkaProducerService, never()).sendPaymentConfirmation(any());
    }

    @Test
    void paymentWebhook_shouldReturnUnauthorized_whenJwtIsInvalid() throws Exception {
        String token = "invalid-token";
        when(jwtService.parseToken(token)).thenThrow(new io.jsonwebtoken.security.SignatureException("Invalid JWT"));

        mockMvc.perform(get("/v1/payments/webhook").param("token", token))
                .andExpect(status().isBadRequest()); // Or whatever PaymentTokenException maps to
    }

    @Test
    void paymentWebhook_shouldReturnConflict_whenPaymentIsAlreadyProcessed() throws Exception {
        String token = "test-token";
        Claims claims = mock(Claims.class);
        when(claims.get(eq("orderId"), eq(Long.class))).thenReturn(order.getId());
        when(claims.get(eq("paymentId"), eq(String.class))).thenReturn("pay_123");
        when(claims.get(eq("paymentAccepted"), eq(Boolean.class))).thenReturn(true);

        when(jwtService.parseToken(token)).thenReturn(claims);

        // First call
        mockMvc.perform(get("/v1/payments/webhook").param("token", token))
                .andExpect(status().isOk());

        // Second call
        mockMvc.perform(get("/v1/payments/webhook").param("token", token))
                .andExpect(status().isConflict()); // Or whatever PaymentAlreadyExistsException maps to
    }
}
