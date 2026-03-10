package com.subito.subitocodingtest.controller;

import com.subito.subitocodingtest.dto.BasketItemRequest;
import com.subito.subitocodingtest.dto.BasketResponse;
import com.subito.subitocodingtest.model.BasketStatus;
import com.subito.subitocodingtest.service.BasketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/users/{userId}/baskets")
@RequiredArgsConstructor
public class BasketController {
    private final BasketService basketService;

    @PostMapping
    public BasketResponse createBasket(@PathVariable String userId) {
        return basketService.createBasket(userId);
    }

    @GetMapping
    public List<BasketResponse> getBaskets(@PathVariable String userId,
                                          @RequestParam(required = false) BasketStatus status) {
        return basketService.getBaskets(userId, status);
    }

    @PostMapping("/{basketId}/products")
    public BasketResponse addItemToBasket(@PathVariable Long basketId,
                                          @Valid @RequestBody BasketItemRequest itemRequest) {
        return basketService.addItemToBasket(basketId, itemRequest);
    }

    @DeleteMapping("/{basketId}/products/{productId}")
    public BasketResponse removeItemFromBasket(@PathVariable Long basketId,
                                               @PathVariable Long productId) {
        return basketService.removeItemFromBasket(basketId, productId);
    }

    @DeleteMapping("/{basketId}")
    public void deleteBasket(@PathVariable Long basketId) {
        basketService.deleteBasket(basketId);
    }
}


