package com.subito.subitocodingtest.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
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

    @Column(nullable = false)
    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.INSERTED;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_info_id", nullable = false)
    @Valid
    @NotNull(message = "User info cannot be null")
    private UserInfo userInfo;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "shipping_info_id", nullable = false)
    @Valid
    @NotNull(message = "Shipping info cannot be null")
    private ShippingInfo shippingInfo;

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

