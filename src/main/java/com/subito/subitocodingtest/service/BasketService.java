package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.dto.BasketItemRequest;
import com.subito.subitocodingtest.dto.BasketResponse;
import com.subito.subitocodingtest.exception.BasketAlreadyExistsException;
import com.subito.subitocodingtest.exception.BasketStatusException;
import com.subito.subitocodingtest.exception.ResourceNotFoundException;
import com.subito.subitocodingtest.exception.ResourceType;
import com.subito.subitocodingtest.model.Basket;
import com.subito.subitocodingtest.model.BasketItem;
import com.subito.subitocodingtest.model.BasketStatus;
import com.subito.subitocodingtest.model.Product;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BasketService {

    @Transactional
    BasketResponse createBasket(String userId);

    @Transactional(readOnly = true)
    List<BasketResponse> getBaskets(String userId, BasketStatus status);

    @Transactional
    BasketResponse addItemToBasket(Long basketId, BasketItemRequest itemRequest);

    @Transactional
    BasketResponse removeItemFromBasket(Long basketId, Long productId);

    @Transactional
    void deleteBasket(Long basketId);
}
