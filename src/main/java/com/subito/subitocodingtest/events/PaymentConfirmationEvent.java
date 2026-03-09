package com.subito.subitocodingtest.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PaymentConfirmationEvent extends OrderEvent {
    private String paymentId;
    private Long orderId;
    private String userEmail;
}
