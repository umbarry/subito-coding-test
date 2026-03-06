package com.subito.subitocodingtest.exception;

import lombok.Getter;

@Getter
public enum ResourceType {
    ORDER("Order"),
    PRODUCT("Product"),
    BASKET("Basket"),
    BASKET_ITEM("Basket Item");

    private final String displayName;

    ResourceType(String displayName) {
        this.displayName = displayName;
    }

}

