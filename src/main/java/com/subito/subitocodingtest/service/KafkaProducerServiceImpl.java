package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.events.OrderExpirationEvent;
import com.subito.subitocodingtest.events.OrderShippedEvent;
import com.subito.subitocodingtest.events.PaymentConfirmationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topic.order.shipped}")
    private String orderShippedTopic;

    @Value("${topic.payment.confirmation}")
    private String paymentTopic;

    @Value("${topic.order.expiration}")
    private String expirationTopic;

    public KafkaProducerServiceImpl(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void sendPaymentConfirmation(PaymentConfirmationEvent event) {
        log.info("Sending payment confirmation event for order ID: {}", event.getOrderId());
        kafkaTemplate.send(paymentTopic, event);
    }

    @Override
    public void sendOrderExpiration(OrderExpirationEvent event) {
        log.info("Sending order expiration event for order ID: {}", event.getOrderId());
        kafkaTemplate.send(expirationTopic, event);
    }

    @Override
    public void sendOrderShipped(OrderShippedEvent event) {
        log.info("Sending order shipped event for order ID: {}", event.getOrderId());
        kafkaTemplate.send(orderShippedTopic, event);
    }
}
