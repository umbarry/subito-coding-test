package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.dto.CreateOrderRequest;
import com.subito.subitocodingtest.dto.OrderResponse;
import com.subito.subitocodingtest.events.OrderExpirationEvent;
import com.subito.subitocodingtest.events.OrderShippedEvent;
import com.subito.subitocodingtest.exception.ResourceNotFoundException;
import com.subito.subitocodingtest.exception.ResourceType;
import com.subito.subitocodingtest.model.*;
import com.subito.subitocodingtest.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final BasketRepository basketRepository;
    private final KafkaProducerService kafkaProducerService;

    @Value("${order.expiration.time.minutes}")
    private int orderExpirationTimeMinutes;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Long basketId = request.getBasketId();
        log.info("Creating order for basketId: {}", basketId);

        // Retrieve pending basket for user
        Basket basket = basketRepository.findById(basketId)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.BASKET, basketId));

        if (basket.getStatus() != BasketStatus.PENDING || basket.getItems().isEmpty()) {
            log.warn("Basket is not pending or is empty for basketId: {}", basketId);
            throw new IllegalArgumentException("Basket is not pending or is empty for basketId: " + basketId);
        }

        log.debug("Found pending basket ID: {} with {} items", basket.getId(), basket.getItems().size());

        Order order = new Order();
        order.setBasket(basket);

        // Set user and shipping info directly on order
        order.setName(request.getName());
        order.setLastName(request.getLastName());
        order.setEmail(request.getEmail());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setStreet(request.getStreet());
        order.setCity(request.getCity());
        order.setPostalCode(request.getPostalCode());
        order.setCountry(request.getCountry());
        log.debug("Set user and shipping info for order: {} {} {} {} {} {} {} {}", request.getName(), request.getLastName(), request.getEmail(), request.getPhoneNumber(), request.getStreet(), request.getCity(), request.getPostalCode(), request.getCountry());

        // Process items from basket and update product availability
        for (BasketItem basketItem : basket.getItems()) {
            Product product = basketItem.getProduct();
            int quantity = basketItem.getQuantity();

            log.debug("Processing product: {} with quantity: {}", product.getName(), quantity);

            // Check availability
            if (product.getAvailableItems() < quantity) {
                log.error("Insufficient inventory for product: {}. Available: {}, Requested: {}",
                        product.getName(), product.getAvailableItems(), quantity);
                throw new IllegalArgumentException("Not enough items available for product: " + product.getName());
            }

            // Create order item with prices
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(quantity)
                    .unitNetPrice(product.getNetPrice())
                    .build();

            // Calculate price and VAT
            BigDecimal itemPrice = product.getNetPrice().multiply(new BigDecimal(quantity));
            BigDecimal itemVat = itemPrice.multiply(product.getVatPercentage()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);

            orderItem.setPrice(itemPrice);
            orderItem.setVat(itemVat);

            order.addItem(orderItem);
            log.debug("Added item to order - Product: {}, Price: {}, VAT: {}", product.getName(), itemPrice, itemVat);

            // Update product availability (optimistic lock)
            product.setAvailableItems(product.getAvailableItems() - quantity);
            productRepository.save(product);
            log.debug("Updated product availability for: {}. New available items: {}", product.getName(), product.getAvailableItems());
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with ID: {}. Total items: {}, Grand total: {}",
                savedOrder.getId(), savedOrder.getItems().size(), savedOrder.getGrandTotal());

        // Mark basket as completed
        basket.setStatus(BasketStatus.COMPLETED);
        basketRepository.save(basket);
        log.debug("Basket ID: {} marked as COMPLETED", basket.getId());

        return OrderResponse.fromOrder(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        log.info("Retrieving order with ID: {}", orderId);

        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            log.warn("Order not found with ID: {}", orderId);
            throw new ResourceNotFoundException(ResourceType.ORDER, orderId);
        }

        log.debug("Order found with {} items", order.get().getItems().size());
        return OrderResponse.fromOrder(order.get());
    }

    @Transactional
    public void expireUnpaidOrders() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(orderExpirationTimeMinutes);
        List<Order> unpaidOrders = orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.INSERTED, expirationTime);

        for (Order order : unpaidOrders) {
            log.info("Expiring order with ID: {}", order.getId());
            order.setStatus(OrderStatus.EXPIRED);

            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setAvailableItems(product.getAvailableItems() + item.getQuantity());
                productRepository.save(product);
            }

            orderRepository.save(order);
            kafkaProducerService.sendOrderExpiration(new OrderExpirationEvent(order.getId(), order.getEmail()));
        }
    }

    @Transactional
    public OrderResponse shipOrder(Long orderId, String trackingUrl) {
        log.info("Shipping order with ID: {} with tracking URL: {}", orderId, trackingUrl);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.ORDER, orderId));

        if (order.getStatus() != OrderStatus.PAID) {
            log.warn("Cannot ship order ID: {} with status: {}", orderId, order.getStatus());
            throw new IllegalArgumentException("Order must be PAID to be shipped. Current status: " + order.getStatus());
        }

        // Update order status and set tracking URL
        order.setStatus(OrderStatus.SHIPPED);
        order.setTrackingUrl(trackingUrl);
        Order shippedOrder = orderRepository.save(order);
        log.info("Order ID: {} marked as SHIPPED with tracking URL: {}", orderId, trackingUrl);

        // Send order shipped event
        kafkaProducerService.sendOrderShipped(new OrderShippedEvent(order.getId(), order.getEmail(), trackingUrl));

        return OrderResponse.fromOrder(shippedOrder);
    }
}
