package com.subito.subitocodingtest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketItemRequest {
    private Long productId;
    private Integer quantity;
}

