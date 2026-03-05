package com.subito.subitocodingtest.dto;

import com.subito.subitocodingtest.model.Order;
import com.subito.subitocodingtest.model.OrderItem;
import com.subito.subitocodingtest.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private OrderStatus status;
    private LocalDateTime insertDate;
    private UserInfoResponse userInfo;
    private ShippingInfoResponse shippingInfo;
    private BigDecimal totalPrice;
    private BigDecimal totalVat;
    private BigDecimal grandTotal;
    private List<OrderItemResponse> items;

    public static OrderResponse fromOrder(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setStatus(order.getStatus());
        response.setInsertDate(order.getCreatedAt());
        response.setUserInfo(UserInfoResponse.fromUserInfo(order.getUserInfo()));
        response.setShippingInfo(ShippingInfoResponse.fromShippingInfo(order.getShippingInfo()));
        response.setTotalPrice(order.getTotalPrice());
        response.setTotalVat(order.getTotalVat());
        response.setGrandTotal(order.getGrandTotal());
        response.setItems(order.getItems().stream()
                .map(OrderItemResponse::fromOrderItem)
                .collect(Collectors.toList()));
        return response;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitNetPrice;
        private BigDecimal price;
        private BigDecimal vat;
        private BigDecimal total;

        public static OrderItemResponse fromOrderItem(OrderItem item) {
            OrderItemResponse response = new OrderItemResponse();
            response.setProductId(item.getProduct().getId());
            response.setProductName(item.getProduct().getName());
            response.setQuantity(item.getQuantity());
            response.setUnitNetPrice(item.getUnitNetPrice());
            response.setPrice(item.getPrice());
            response.setVat(item.getVat());
            response.setTotal(item.getTotalPrice());
            return response;
        }
    }
}

