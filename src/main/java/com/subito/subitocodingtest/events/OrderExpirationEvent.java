package com.subito.subitocodingtest.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderExpirationEvent {
    private Long orderId;
    private String userEmail;
}
