package com.subito.subitocodingtest.controller;

import com.subito.subitocodingtest.dto.CreateOrderRequest;
import com.subito.subitocodingtest.dto.OrderResponse;
import com.subito.subitocodingtest.dto.ShipOrderRequest;
import com.subito.subitocodingtest.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    @PostMapping("/{orderId}/ship")
    public OrderResponse shipOrder(@PathVariable Long orderId, @Valid @RequestBody ShipOrderRequest request) {
        return orderService.shipOrder(orderId, request);
    }
}
