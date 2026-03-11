package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.events.PaymentConfirmationEvent;
import com.subito.subitocodingtest.exception.PaymentAlreadyExistsException;
import com.subito.subitocodingtest.exception.PaymentTokenException;
import com.subito.subitocodingtest.exception.ResourceNotFoundException;
import com.subito.subitocodingtest.exception.ResourceType;
import com.subito.subitocodingtest.model.Order;
import com.subito.subitocodingtest.model.OrderStatus;
import com.subito.subitocodingtest.model.Payment;
import com.subito.subitocodingtest.model.PaymentStatus;
import com.subito.subitocodingtest.repository.OrderRepository;
import com.subito.subitocodingtest.repository.PaymentRepository;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final JwtService jwtService;
    private final KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
    public void processPayment(String token) {
        try {
            Claims claims = jwtService.parseToken(token);

            String paymentId = claims.get("paymentId", String.class);
            Long orderId = claims.get("orderId", Long.class);
            boolean paymentAccepted = claims.get("paymentAccepted", Boolean.class);

            if (paymentRepository.findByPaymentId(paymentId).isPresent()) {
                log.info("Payment with ID {} has already been processed.", paymentId);
                throw new PaymentAlreadyExistsException("Payment with ID " + paymentId + " has already been processed.");
            }

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ORDER, orderId));

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
        } catch (SignatureException | ExpiredJwtException | MalformedJwtException | UnsupportedJwtException e) {
            // Firma non matcha JWT_SECRET_WEBHOOK
            throw new PaymentTokenException("Invalid JWT signature");
        }
    }
}
