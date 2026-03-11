package com.subito.subitocodingtest.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OrderShippedEvent extends OrderEvent {
    private Long orderId;
    private String userEmail;
    private String trackingUrl;
}
