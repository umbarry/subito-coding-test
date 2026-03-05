package com.subito.subitocodingtest.service;

import com.subito.subitocodingtest.dto.BasketItemRequest;
import com.subito.subitocodingtest.dto.BasketResponse;
import com.subito.subitocodingtest.exception.BasketAlreadyExistsException;
import com.subito.subitocodingtest.exception.ResourceNotFoundException;
import com.subito.subitocodingtest.exception.ResourceType;
import com.subito.subitocodingtest.model.Basket;
import com.subito.subitocodingtest.model.BasketItem;
import com.subito.subitocodingtest.model.BasketStatus;
import com.subito.subitocodingtest.model.Product;
import com.subito.subitocodingtest.repository.BasketRepository;
import com.subito.subitocodingtest.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BasketService {
    private final BasketRepository basketRepository;
    private final ProductRepository productRepository;

    public BasketService(BasketRepository basketRepository, ProductRepository productRepository) {
        this.basketRepository = basketRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public BasketResponse createBasket(String userId) {
        log.info("Creating basket for userId: {}", userId);

        // Check if a pending basket already exists for this user
        List<Basket> existingBasket = basketRepository.findByUserIdAndStatus(userId, BasketStatus.PENDING);
        if (!existingBasket.isEmpty()) {
            log.warn("Pending basket already exists for userId: {} with basketId: {}", userId, existingBasket.get(0).getId());
            throw new BasketAlreadyExistsException("A pending basket already exists for user: " + userId);
        }

        Basket basket = Basket.builder().userId(userId).status(BasketStatus.PENDING).build();
        Basket savedBasket = basketRepository.save(basket);
        log.debug("Basket created with ID: {} for userId: {}", savedBasket.getId(), userId);
        return BasketResponse.fromBasket(savedBasket);
    }

    @Transactional(readOnly = true)
    public List<BasketResponse> getBaskets(String userId, BasketStatus status) {
        log.info("Retrieving basket of user with ID: {} - Status filter: {}", userId, status);
        List<Basket> basketList = basketRepository.findByUserIdAndStatus(userId, status);

        if(basketList.isEmpty()) {
            log.warn("No basket found for userId: {} with status: {}", userId, status);
            return Collections.emptyList();
        }


        return basketList.stream().map(BasketResponse::fromBasket).toList();
    }

    @Transactional
    public BasketResponse addItemToBasket(Long basketId, BasketItemRequest itemRequest) {
        log.info("Adding item to basket ID: {} - Product ID: {}, Quantity: {}", basketId, itemRequest.getProductId(), itemRequest.getQuantity());

        Optional<Basket> basketOpt = basketRepository.findById(basketId);
        if (basketOpt.isEmpty()) {
            log.warn("Basket not found with ID: {}", basketId);
            throw new ResourceNotFoundException(ResourceType.BASKET, basketId);
        }

        Optional<Product> productOpt = productRepository.findById(itemRequest.getProductId());
        if (productOpt.isEmpty()) {
            log.warn("Product not found with ID: {}", itemRequest.getProductId());
            throw new ResourceNotFoundException(ResourceType.PRODUCT, itemRequest.getProductId());
        }

        Basket basket = basketOpt.get();
        Product product = productOpt.get();

        BasketItem basketItem = BasketItem.builder()
                .basket(basket)
                .product(product)
                .quantity(itemRequest.getQuantity())
                .build();

        // if already in basket then update quantity
        Optional<BasketItem> existingItemOpt = basket.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            BasketItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + itemRequest.getQuantity());
            log.debug("Product already in basket, updated quantity to {}", existingItem.getQuantity());
        } else {
            basket.addItem(basketItem);
        }

        Basket savedBasket = basketRepository.save(basket);
        log.debug("Item added successfully. Basket now contains {} items", savedBasket.getItems().size());
        return BasketResponse.fromBasket(savedBasket);
    }

    @Transactional
    public BasketResponse removeItemFromBasket(Long basketId, Long basketItemId) {
        log.info("Removing item {} from basket ID: {}", basketItemId, basketId);

        Optional<Basket> basketOpt = basketRepository.findById(basketId);
        if (basketOpt.isEmpty()) {
            log.warn("Basket not found with ID: {}", basketId);
            throw new ResourceNotFoundException(ResourceType.BASKET, basketId);
        }

        Basket basket = basketOpt.get();
        basket.getItems().removeIf(item -> item.getId().equals(basketItemId));
        Basket savedBasket = basketRepository.save(basket);
        log.debug("Item removed. Basket now contains {} items", savedBasket.getItems().size());
        return BasketResponse.fromBasket(savedBasket);
    }

    @Transactional
    public void deleteBasket(Long basketId) {
        log.info("Deleting basket with ID: {}", basketId);
        Optional<Basket> basketOpt = basketRepository.findById(basketId);
        if (basketOpt.isEmpty()) {
            log.warn("Basket not found with ID: {}", basketId);
            throw new ResourceNotFoundException(ResourceType.BASKET, basketId);
        }
        basketRepository.deleteById(basketId);
        log.debug("Basket deleted successfully");
    }
}

