package com.subito.subitocodingtest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotNull(message = "User ID cannot be null")
    private String userId;

    @Valid
    @NotNull(message = "User info cannot be null")
    private UserInfoRequest userInfo;

    @Valid
    @NotNull(message = "Shipping info cannot be null")
    private ShippingInfoRequest shippingInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Product ID cannot be null")
        private Long productId;

        @NotNull(message = "Quantity cannot be null")
        private Integer quantity;
    }
}

