package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.events.OrderExpirationEvent;
import com.subito.subitocodingtest.events.OrderShippedEvent;
import com.subito.subitocodingtest.events.PaymentConfirmationEvent;
import org.springframework.transaction.annotation.Transactional;

public interface KafkaConsumerService {
    @Transactional
    void consumePaymentConfirmation(PaymentConfirmationEvent event);

    @Transactional
    void consumeOrderExpiration(OrderExpirationEvent event);

    @Transactional
    void consumeOrderShipped(OrderShippedEvent event);
}
