package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.events.PaymentConfirmationEvent;
import com.subito.subitocodingtest.exception.ResourceNotFoundException;
import com.subito.subitocodingtest.exception.ResourceType;
import com.subito.subitocodingtest.model.Order;
import com.subito.subitocodingtest.model.OrderStatus;
import com.subito.subitocodingtest.model.Payment;
import com.subito.subitocodingtest.model.PaymentStatus;
import com.subito.subitocodingtest.repository.OrderRepository;
import com.subito.subitocodingtest.repository.PaymentRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final JwtService jwtService;
    private final KafkaProducerService kafkaProducerService;

    public PaymentService(OrderRepository orderRepository, PaymentRepository paymentRepository, JwtService jwtService, KafkaProducerService kafkaProducerService) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.jwtService = jwtService;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Transactional
    public void processPayment(String token) {
        Claims claims = jwtService.parseToken(token);

        String paymentId = claims.get("paymentId", String.class);
        Long orderId = claims.get("orderId", Long.class);
        boolean paymentAccepted = claims.get("paymentAccepted", Boolean.class);

        if (paymentRepository.findByPaymentId(paymentId).isPresent()) {
            log.info("Payment with ID {} has already been processed.", paymentId);
            return;
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ORDER, orderId));

        // TODO:chek payment amount with order price
        //if(!order.getTotalPrice().equals())

        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .order(order)
                .status(paymentAccepted ? PaymentStatus.ACCEPTED : PaymentStatus.REJECTED)
                .build();

        paymentRepository.save(payment);

        if (paymentAccepted) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
            kafkaProducerService.sendPaymentConfirmation(new PaymentConfirmationEvent(paymentId, order.getId(), order.getEmail()));
        }
    }

    @Service
    public static class JwtService {

        @Value("${jwt.secret}")
        private String secret;

        public Claims parseToken(String token) {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }
    }
}
