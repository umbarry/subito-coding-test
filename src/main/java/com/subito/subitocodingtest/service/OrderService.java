package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.dto.CreateOrderRequest;
import com.subito.subitocodingtest.dto.OrderResponse;
import com.subito.subitocodingtest.dto.ShipOrderRequest;
import org.springframework.transaction.annotation.Transactional;

public interface OrderService {
    @Transactional
    OrderResponse createOrder(CreateOrderRequest request);

    @Transactional(readOnly = true)
    OrderResponse getOrder(Long orderId);

    @Transactional
    void expireUnpaidOrders();

    @Transactional
    OrderResponse shipOrder(Long orderId, ShipOrderRequest request);
}
