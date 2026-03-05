package com.subito.subitocodingtest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "Product name cannot be blank")
    private String name;

    @Column(nullable = false)
    @NotNull(message = "Net price cannot be null")
    @PositiveOrZero(message = "Net price must be positive or zero")
    private BigDecimal netPrice;

    @Column(nullable = false)
    @NotNull(message = "VAT percentage cannot be null")
    @PositiveOrZero(message = "VAT percentage must be positive or zero")
    private BigDecimal vatPercentage;

    @Column(nullable = false)
    @NotNull(message = "Available items cannot be null")
    @PositiveOrZero(message = "Available items must be positive or zero")
    private Integer availableItems;

    @Version
    private Long version;
}




