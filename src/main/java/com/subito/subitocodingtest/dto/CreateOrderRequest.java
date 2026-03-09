package com.subito.subitocodingtest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    @NotNull(message = "Basket ID cannot be null")
    private Long basketId;

    // Flattened user info fields
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid and contain @ and .")
    private String email;

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\+?[0-9]+$", message = "Phone number must be numeric and can start with +")
    private String phoneNumber;

    // Flattened shipping info fields
    @NotBlank(message = "Street cannot be blank")
    private String street;

    @NotBlank(message = "City cannot be blank")
    private String city;

    @NotBlank(message = "Postal code cannot be blank")
    @Pattern(regexp = "^[0-9]+$", message = "Postal code must be numeric")
    private String postalCode;

    @NotBlank(message = "Country cannot be blank")
    private String country;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Product ID cannot be null")
        private Long productId;

        @NotNull(message = "Quantity cannot be null")
        private Integer quantity;
    }
}
