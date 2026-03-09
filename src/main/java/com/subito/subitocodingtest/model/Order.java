package com.subito.subitocodingtest.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "basket_id")
    private Basket basket;

    @Column(nullable = false)
    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.INSERTED;

    // User info fields
    @Column(nullable = false)
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Column(nullable = false)
    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @Column(nullable = false)
    @NotBlank(message = "Email cannot be blank")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Phone number cannot be blank")
    private String phoneNumber;

    // Shipping info fields
    @Column(nullable = false)
    @NotBlank(message = "Street cannot be blank")
    private String street;

    @Column(nullable = false)
    @NotBlank(message = "City cannot be blank")
    private String city;

    @Column(nullable = false)
    @NotBlank(message = "Postal code cannot be blank")
    private String postalCode;

    @Column(nullable = false)
    @NotBlank(message = "Country cannot be blank")
    private String country;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalVat() {
        return items.stream()
                .map(OrderItem::getVat)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getGrandTotal() {
        return getTotalPrice().add(getTotalVat());
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = OrderStatus.INSERTED;
        }
    }
}
