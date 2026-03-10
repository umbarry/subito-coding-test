package com.subito.subitocodingtest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Builder
@Entity
@Table(name = "basket_items")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BasketItem extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "basket_id", nullable = false)
    @ToString.Exclude
    private Basket basket;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product cannot be null")
    private Product product;

    @Column(nullable = false)
    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private BigDecimal vat;

    private BigDecimal calculateVat() {
        return this.price.multiply(product.getVatPercentage()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalPrice() {
        return this.price.add(this.vat);
    }

    @PrePersist
    @PreUpdate
    public void calculatePriceAndVat() {
        this.price = product.getNetPrice().multiply(new BigDecimal(quantity));
        this.vat = calculateVat();
    }
}

