package com.subito.subitocodingtest.model;

public enum NotificationType {
    PAYMENT_CONFIRMATION("Payment Confirmation"),
    ORDER_EXPIRATION("Order Expiration"),
    ORDER_SHIPPED("Order Shipped");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

