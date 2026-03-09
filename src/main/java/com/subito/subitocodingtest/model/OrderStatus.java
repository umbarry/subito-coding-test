package com.subito.subitocodingtest.model;

public enum OrderStatus {
    INSERTED("Inserted"),
    PAID("Paid"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    EXPIRED("Expired");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
