package com.subito.subitocodingtest.dto;

import com.subito.subitocodingtest.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal netPrice;
    private BigDecimal vatPercentage;
    private Integer availableItems;

    public static ProductResponse fromProduct(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .netPrice(product.getNetPrice())
                .vatPercentage(product.getVatPercentage())
                .availableItems(product.getAvailableItems())
                .build();
    }
}


