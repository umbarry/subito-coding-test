package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.dto.CreateOrderRequest;
import com.subito.subitocodingtest.dto.OrderResponse;
import com.subito.subitocodingtest.exception.ResourceNotFoundException;
import com.subito.subitocodingtest.exception.ResourceType;
import com.subito.subitocodingtest.model.*;
import com.subito.subitocodingtest.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final BasketRepository basketRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String userId = request.getUserId();
        log.info("Creating order for userId: {}", userId);

        // Retrieve pending basket for user
        List<Basket> basketList = basketRepository.findByUserIdAndStatus(userId, BasketStatus.PENDING);
        if (basketList.isEmpty() || basketList.get(0).getItems().isEmpty()) {
            log.warn("Pending basket is empty or not found for userId: {}", userId);
            throw new IllegalArgumentException("No pending basket found for user: " + userId);
        }

        Basket basket = basketList.get(0);
        log.debug("Found pending basket ID: {} with {} items for userId: {}", basket.getId(), basket.getItems().size(), userId);

        Order order = new Order();

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
}
