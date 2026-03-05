package com.subito.subitocodingtest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "shipping_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingInfo extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
}

