package com.subito.subitocodingtest.exception;

import com.subito.subitocodingtest.model.BasketStatus;

public class BasketStatusException extends RuntimeException {
    public BasketStatusException(String message) {
        super(message);
    }

    public BasketStatusException(Long basketId, BasketStatus currentStatus) {
        super(String.format("Basket with ID %d is not in a valid state for this operation. Current state: %s", basketId, currentStatus));
    }
}
