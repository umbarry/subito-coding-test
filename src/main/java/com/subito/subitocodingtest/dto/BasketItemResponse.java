package com.subito.subitocodingtest.dto;

import com.subito.subitocodingtest.model.BasketItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasketItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitNetPrice;
    private BigDecimal price;
    private BigDecimal vat;
    private BigDecimal total;

    public static BasketItemResponse fromBasketItem(BasketItem item) {
        BasketItemResponse response = new BasketItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setQuantity(item.getQuantity());
        response.setUnitNetPrice(item.getProduct().getNetPrice());
        response.setPrice(item.getPrice());
        response.setVat(item.getVat());
        response.setTotal(item.getTotalPrice());
        return response;
    }
}


