package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.events.OrderExpirationEvent;
import com.subito.subitocodingtest.events.OrderShippedEvent;
import com.subito.subitocodingtest.events.PaymentConfirmationEvent;
import com.subito.subitocodingtest.model.Notification;
import com.subito.subitocodingtest.model.NotificationType;
import com.subito.subitocodingtest.model.Order;
import com.subito.subitocodingtest.model.Payment;
import com.subito.subitocodingtest.repository.NotificationRepository;
import com.subito.subitocodingtest.repository.OrderRepository;
import com.subito.subitocodingtest.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

    private final JavaMailSender mailSender;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;

    @Override
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

            Order order = payment.getOrder();

            // Check if notification already sent using Notifications table
            Notification notification = notificationRepository.findByOrderIdAndType(order.getId(), NotificationType.PAYMENT_CONFIRMATION)
                    .orElse(null);

            if (notification != null && notification.isSent()) {
                log.info("Notification for payment ID {} has already been sent.", event.getPaymentId());
                return;
            }

            // Create or update notification record
            if (notification == null) {
                notification = Notification.builder()
                        .order(order)
                        .type(NotificationType.PAYMENT_CONFIRMATION)
                        .sent(true)
                        .build();
            } else {
                notification.setSent(true);
            }
            notificationRepository.saveAndFlush(notification);

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

    @Override
    @KafkaListener(topics = "${topic.order.expiration}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeOrderExpiration(OrderExpirationEvent event) {
        log.info("Received order expiration event for order ID: {}", event.getOrderId());

        try {
            Order order = orderRepository.findById(event.getOrderId()).orElse(null);

            if (order == null) {
                log.error("Order with ID {} not found.", event.getOrderId());
                return;
            }

            // Check if notification already sent using Notifications table
            Notification notification = notificationRepository.findByOrderIdAndType(order.getId(), NotificationType.ORDER_EXPIRATION)
                    .orElse(null);

            if (notification != null && notification.isSent()) {
                log.info("Expiration notification for order ID {} has already been sent.", event.getOrderId());
                return;
            }

            // Create or update notification record
            if (notification == null) {
                notification = Notification.builder()
                        .order(order)
                        .type(NotificationType.ORDER_EXPIRATION)
                        .sent(true)
                        .build();
            } else {
                notification.setSent(true);
            }
            notificationRepository.saveAndFlush(notification);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@subito.it");
            message.setTo(event.getUserEmail());
            message.setSubject("Order Expired");
            message.setText("Your order " + event.getOrderId() + " has expired because it was not paid in time.");

            mailSender.send(message);
            log.info("Expiration email sent to {}", event.getUserEmail());

        } catch (OptimisticLockingFailureException e) {
            log.warn("Concurrent update detected for order ID: {}. Skipping email sending.", event.getOrderId());
        }
    }

    @Override
    @KafkaListener(topics = "${topic.order.shipped}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeOrderShipped(OrderShippedEvent event) {
        log.info("Received order shipped event for order ID: {}", event.getOrderId());

        try {
            Order order = orderRepository.findById(event.getOrderId()).orElse(null);

            if (order == null) {
                log.error("Order with ID {} not found.", event.getOrderId());
                return;
            }

            // Check if notification already sent using Notifications table
            Notification notification = notificationRepository.findByOrderIdAndType(order.getId(), NotificationType.ORDER_SHIPPED)
                    .orElse(null);

            if (notification != null && notification.isSent()) {
                log.info("Shipped notification for order ID {} has already been sent.", event.getOrderId());
                return;
            }

            // Create or update notification record
            if (notification == null) {
                notification = Notification.builder()
                        .order(order)
                        .type(NotificationType.ORDER_SHIPPED)
                        .sent(true)
                        .build();
            } else {
                notification.setSent(true);
            }
            notificationRepository.saveAndFlush(notification);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("no-reply@subito.it");
            message.setTo(event.getUserEmail());
            message.setSubject("Your Order Has Been Shipped");
            message.setText("Your order " + event.getOrderId() + " has been shipped! Track your package using this URL: " + event.getTrackingUrl());

            mailSender.send(message);
            log.info("Shipped email sent to {}", event.getUserEmail());

        } catch (OptimisticLockingFailureException e) {
            log.warn("Concurrent update detected for order ID: {}. Skipping email sending.", event.getOrderId());
        }
    }
}
