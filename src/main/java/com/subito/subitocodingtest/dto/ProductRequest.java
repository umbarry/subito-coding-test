package com.subito.subitocodingtest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    @NotBlank(message = "Product name cannot be blank")
    private String name;

    @NotNull(message = "Net price cannot be null")
    @PositiveOrZero(message = "Net price must be positive or zero")
    private BigDecimal netPrice;

    @NotNull(message = "VAT percentage cannot be null")
    @PositiveOrZero(message = "VAT percentage must be positive or zero")
    private BigDecimal vatPercentage;

    @NotNull(message = "Available items cannot be null")
    @PositiveOrZero(message = "Available items must be positive or zero")
    private Integer availableItems;
}

