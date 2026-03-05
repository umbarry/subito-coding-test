package com.subito.subitocodingtest.dto;

import com.subito.subitocodingtest.model.Basket;
import com.subito.subitocodingtest.model.BasketItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasketResponse {
    private Long id;
    private BigDecimal totalPrice;
    private BigDecimal totalVat;
    private BigDecimal grandTotal;
    private List<BasketItemResponse> items;

    public static BigDecimal getTotalPrice(List<BasketItem> items) {
        if(isNull(items)) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(BasketItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BigDecimal getTotalVat(List<BasketItem> items) {

        if (isNull(items)) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(BasketItem::getVat)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static BasketResponse fromBasket(Basket basket) {
        BasketResponse response = new BasketResponse();
        response.setId(basket.getId());
        response.setTotalPrice(getTotalPrice(basket.getItems()));
        response.setTotalVat(getTotalVat(basket.getItems()));
        response.setGrandTotal(response.getTotalPrice().add(response.getTotalVat()));

        if(!isNull(basket.getItems())) {
            response.setItems(basket.getItems().stream()
                    .map(BasketItemResponse::fromBasketItem)
                    .collect(Collectors.toList()));
        }

        return response;
    }
}

