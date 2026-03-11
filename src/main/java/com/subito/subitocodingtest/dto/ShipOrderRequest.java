package com.subito.subitocodingtest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipOrderRequest {
    @NotBlank(message = "Tracking URL cannot be blank")
    private String trackingUrl;
}
