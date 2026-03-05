package com.subito.subitocodingtest.model;

public enum BasketStatus {
    PENDING("Pending"),
    COMPLETED("Completed");

    private final String displayName;

    BasketStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

