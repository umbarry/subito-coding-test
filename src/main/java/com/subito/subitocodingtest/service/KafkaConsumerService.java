package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.events.OrderExpirationEvent;
import com.subito.subitocodingtest.events.PaymentConfirmationEvent;
import com.subito.subitocodingtest.model.Payment;
import com.subito.subitocodingtest.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class KafkaConsumerService {

    private final JavaMailSender mailSender;
    private final PaymentRepository paymentRepository;

    public KafkaConsumerService(JavaMailSender mailSender, PaymentRepository paymentRepository) {
        this.mailSender = mailSender;
        this.paymentRepository = paymentRepository;
    }

    @KafkaListener(topics = "${topic.payment.confirmation}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumePaymentConfirmation(PaymentConfirmationEvent event) {
        log.info("Received payment confirmation event for order ID: {}", event.getOrderId());

        try {
            Payment payment = paymentRepository.findByPaymentId(event.getPaymentId()).orElse(null);

            if (payment == null) {
                log.error("Payment with ID {} not found.", event.getPaymentId());
                return;
            }

            if (payment.isNotificationSent()) {
                log.info("Notification for payment ID {} has already been sent.", event.getPaymentId());
                return;
            }

            // Update the flag first to acquire lock
            payment.setNotificationSent(true);
            paymentRepository.saveAndFlush(payment);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@subito.it");
            message.setTo(event.getUserEmail());
            message.setSubject("Payment Confirmation");
            message.setText("Your payment for order " + event.getOrderId() + " has been successfully processed.");

            mailSender.send(message);
            log.info("Confirmation email sent to {}", event.getUserEmail());

        } catch (OptimisticLockingFailureException e) {
            log.warn("Concurrent update detected for payment ID: {}. Skipping email sending.", event.getPaymentId());
        }
    }

    @KafkaListener(topics = "${topic.order.expiration}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeOrderExpiration(OrderExpirationEvent event) {
        log.info("Received order expiration event for order ID: {}", event.getOrderId());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@subito.it");
        message.setTo(event.getUserEmail());
        message.setSubject("Order Expired");
        message.setText("Your order " + event.getOrderId() + " has expired because it was not paid in time.");

        mailSender.send(message);
        log.info("Expiration email sent to {}", event.getUserEmail());
    }
}
