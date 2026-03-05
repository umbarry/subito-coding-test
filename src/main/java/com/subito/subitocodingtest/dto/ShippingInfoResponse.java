package com.subito.subitocodingtest.dto;

import com.subito.subitocodingtest.model.ShippingInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingInfoResponse {
    private Long id;
    private String street;
    private String city;
    private String postalCode;
    private String country;

    public static ShippingInfoResponse fromShippingInfo(ShippingInfo shippingInfo) {
        return ShippingInfoResponse.builder()
                .id(shippingInfo.getId())
                .street(shippingInfo.getStreet())
                .city(shippingInfo.getCity())
                .postalCode(shippingInfo.getPostalCode())
                .country(shippingInfo.getCountry())
                .build();
    }
}


