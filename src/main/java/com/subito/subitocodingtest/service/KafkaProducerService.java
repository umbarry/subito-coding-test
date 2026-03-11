package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.events.OrderExpirationEvent;
import com.subito.subitocodingtest.events.OrderShippedEvent;
import com.subito.subitocodingtest.events.PaymentConfirmationEvent;

public interface KafkaProducerService {
    void sendPaymentConfirmation(PaymentConfirmationEvent event);
    void sendOrderExpiration(OrderExpirationEvent event);
    void sendOrderShipped(OrderShippedEvent event);
}
