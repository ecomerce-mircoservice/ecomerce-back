package com.example.order_service.dto;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ShippingAddress {

    private String fullName;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
