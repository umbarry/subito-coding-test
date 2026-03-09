package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.events.OrderExpirationEvent;
import com.subito.subitocodingtest.events.PaymentConfirmationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${topic.order.events}")
    private String orderEventsTopic;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPaymentConfirmation(PaymentConfirmationEvent event) {
        log.info("Sending payment confirmation event for order ID: {}", event.getOrderId());
        kafkaTemplate.send(orderEventsTopic, event);
    }

    public void sendOrderExpiration(OrderExpirationEvent event) {
        log.info("Sending order expiration event for order ID: {}", event.getOrderId());
        kafkaTemplate.send(orderEventsTopic, event);
    }
}
