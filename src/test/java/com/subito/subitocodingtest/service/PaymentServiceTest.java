package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.events.PaymentConfirmationEvent;
import com.subito.subitocodingtest.exception.PaymentAlreadyExistsException;
import com.subito.subitocodingtest.exception.PaymentTokenException;
import com.subito.subitocodingtest.model.Order;
import com.subito.subitocodingtest.model.OrderStatus;
import com.subito.subitocodingtest.model.Payment;
import com.subito.subitocodingtest.repository.OrderRepository;
import com.subito.subitocodingtest.repository.PaymentRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentService.JwtService jwtService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void processPayment_shouldUpdateOrderStatusToPaidWhenPaymentIsAccepted() {
        String token = "test-token";
        Claims claims = mock(Claims.class);
        when(claims.get("orderId", Long.class)).thenReturn(1L);
        when(claims.get("paymentId", String.class)).thenReturn("pay_123");
        when(claims.get("paymentAccepted", Boolean.class)).thenReturn(true);

        Order order = Order.builder().id(1L).status(OrderStatus.INSERTED).build();

        when(jwtService.parseToken(token)).thenReturn(claims);
        when(paymentRepository.findByPaymentId("pay_123")).thenReturn(Optional.empty());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.processPayment(token);

        verify(orderRepository).save(argThat(savedOrder -> savedOrder.getStatus() == OrderStatus.PAID));
        verify(kafkaProducerService).sendPaymentConfirmation(any(PaymentConfirmationEvent.class));
    }

    @Test
    void processPayment_shouldNotUpdateOrderStatus_whenPaymentIsRejected() {
        String token = "test-token";
        Claims claims = mock(Claims.class);
        when(claims.get("orderId", Long.class)).thenReturn(1L);
        when(claims.get("paymentId", String.class)).thenReturn("pay_123");
        when(claims.get("paymentAccepted", Boolean.class)).thenReturn(false);

        Order order = Order.builder().id(1L).status(OrderStatus.INSERTED).build();

        when(jwtService.parseToken(token)).thenReturn(claims);
        when(paymentRepository.findByPaymentId("pay_123")).thenReturn(Optional.empty());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        paymentService.processPayment(token);

        verify(orderRepository, never()).save(any());
        verify(kafkaProducerService, never()).sendPaymentConfirmation(any());
    }

    @Test
    void processPayment_shouldThrowException_whenJwtIsInvalid() {
        String token = "invalid-token";
        when(jwtService.parseToken(token)).thenThrow(new SignatureException("Invalid JWT"));

        assertThrows(PaymentTokenException.class, () -> paymentService.processPayment(token));
    }

    @Test
    void processPayment_shouldBeIdempotent_whenPaymentIsAlreadyProcessed() {
        String token = "test-token";
        Claims claims = mock(Claims.class);
        when(claims.get(eq("orderId"), eq(Long.class))).thenReturn(1L);
        when(claims.get(eq("paymentId"), eq(String.class))).thenReturn("pay_123");
        when(claims.get(eq("paymentAccepted"), eq(Boolean.class))).thenReturn(true);

        when(jwtService.parseToken(token)).thenReturn(claims);
        when(paymentRepository.findByPaymentId("pay_123")).thenReturn(Optional.of(new Payment()));

        assertThrows(PaymentAlreadyExistsException.class, () -> paymentService.processPayment(token));

        verify(orderRepository, never()).save(any());
        verify(kafkaProducerService, never()).sendPaymentConfirmation(any());
    }
}
